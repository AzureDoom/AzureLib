package mod.azure.azurelib.common.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Predicate;

import mod.azure.azurelib.common.render.layer.AzRenderLayer;

/**
 * Sorts all of one animatable's translucent geometry back to front, across every pass that draws it: the model, its
 * render layers, re-renders and bones with their own render type.
 * <p>
 * Vanilla already sorts quads inside each translucent batch, but each pass is its own batch and batches are drawn in
 * the order they were submitted. So when an animatable draws translucent geometry in more than one pass (a translucent
 * shell layer over a translucent body, say), whichever pass is drawn first wins: it writes depth, and translucent
 * geometry behind it from a later pass is hidden instead of blended. This sorter sits between the pipeline and the real
 * {@link MultiBufferSource} for the duration of one render, captures the quads of every render type matching its filter
 * instead of drawing them, then replays them all at the end, farthest first, switching render type only where needed.
 * <p>
 * Render types that don't match the filter (solid and cutout geometry, glints, outlines) go straight through and draw
 * as usual, before the sorted translucent geometry.
 * <p>
 * Layers can opt out with {@link AzRenderLayer#sortsTranslucency()}: their translucent geometry is still held back, but
 * drawn in submission order after the sorted geometry, as it is today. Overlays drawn on top of the same surfaces (glow
 * masks, for example) should opt out, since sorting coplanar quads from two passes interleaves them and costs a draw
 * call per switch.
 * <p>
 * Limits:
 * <ul>
 * <li>This orders one animatable's geometry. Separate entities still draw in the order the game renders them.</li>
 * <li>Render types backed by one of the game's fixed buffers draw at the end of the frame regardless of submission
 * order, so their position in the sorted order can't be honored. Entity translucent render types use the shared
 * buffer.</li>
 * <li>Geometry written straight to a {@link VertexConsumer} handed to the pipeline (items and armor pass their own,
 * with glint) bypasses the sorter; only what is requested from the buffer source is sorted.</li>
 * </ul>
 * One instance per pipeline, reused every render; capturing allocates nothing once its buffers have grown. Render
 * thread only.
 */
public final class AzTranslucencySorter implements MultiBufferSource {

    /**
     * Quad-based render types that vanilla sorts on upload, i.e. the translucent ones.
     */
    public static final Predicate<RenderType> DEFAULT_FILTER = renderType -> renderType.sortOnUpload()
        && renderType.mode() == VertexFormat.Mode.QUADS;

    /** x, y, z, color, u, v, uv1 (overlay), uv2 (light), nx, ny, nz */
    private static final int INTS_PER_VERTEX = 11;

    private static final int INTS_PER_QUAD = INTS_PER_VERTEX * 4;

    private static final int SORT_DISTANCE = 0;

    private static final int SORT_ORTHOGRAPHIC = 1;

    private static final int SORT_CUSTOM = 2;

    private MultiBufferSource delegate;

    private Predicate<RenderType> filter = DEFAULT_FILTER;

    private boolean active;

    private int unsortedDepth;

    private int sortMode;

    private VertexSorting customSorting;

    private final List<Capture> capturePool = new ArrayList<>();

    private int captureCount;

    private final IdentityHashMap<RenderType, Capture> sortedCaptures = new IdentityHashMap<>();

    private final IdentityHashMap<RenderType, Capture> unsortedCaptures = new IdentityHashMap<>();

    private int[] vertices = new int[INTS_PER_QUAD * 64];

    private int[] quadCapture = new int[64];

    private boolean[] quadSorted = new boolean[64];

    private float[] quadKeys = new float[64];

    private float[] quadCentroids = new float[64 * 3];

    private int quadCount;

    private int[] order = new int[64];

    private int[] scratch = new int[64];

    /**
     * Starts capturing for one render.
     *
     * @param delegate the buffer source the sorted geometry is finally drawn into
     * @param filter   which render types to sort; others pass straight through
     */
    public void begin(MultiBufferSource delegate, Predicate<RenderType> filter) {
        if (active) {
            throw new IllegalStateException("Translucency sorter is already capturing");
        }

        this.delegate = delegate;
        this.filter = filter;
        this.active = true;
        this.unsortedDepth = 0;

        VertexSorting sorting = RenderSystem.getVertexSorting();

        if (sorting == VertexSorting.DISTANCE_TO_ORIGIN) {
            this.sortMode = SORT_DISTANCE;
        } else if (sorting == VertexSorting.ORTHOGRAPHIC_Z) {
            this.sortMode = SORT_ORTHOGRAPHIC;
        } else {
            this.sortMode = SORT_CUSTOM;
            this.customSorting = sorting;
        }
    }

    /**
     * Draws everything captured, back to front, then stops capturing.
     */
    public void end() {
        try {
            if (active) {
                flush();
            }
        } finally {
            reset();
        }
    }

    /**
     * Stops capturing and throws away what was captured, e.g. after a render failed partway.
     */
    public void abort() {
        reset();
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Until the matching {@link #popUnsorted()}, translucent geometry is held back but not sorted: it draws after the
     * sorted geometry, in submission order.
     */
    public void pushUnsorted() {
        unsortedDepth++;
    }

    public void popUnsorted() {
        if (unsortedDepth > 0) {
            unsortedDepth--;
        }
    }

    /**
     * @return the buffer source sorted geometry is drawn into
     */
    public MultiBufferSource delegate() {
        return delegate;
    }

    @Override
    public @NotNull VertexConsumer getBuffer(@NotNull RenderType renderType) {
        if (!active || !filter.test(renderType)) {
            return delegate.getBuffer(renderType);
        }

        boolean sorted = unsortedDepth == 0;
        IdentityHashMap<RenderType, AzTranslucencySorter.Capture> captures = sorted ? sortedCaptures : unsortedCaptures;
        AzTranslucencySorter.Capture capture = captures.get(renderType);

        if (capture == null) {
            capture = obtainCapture(renderType, sorted);
            captures.put(renderType, capture);
        }

        return capture;
    }

    private Capture obtainCapture(RenderType renderType, boolean sorted) {
        Capture capture;

        if (captureCount < capturePool.size()) {
            capture = capturePool.get(captureCount);
        } else {
            capture = new Capture();
            capturePool.add(capture);
        }

        capture.reset(renderType, sorted, captureCount);
        captureCount++;
        return capture;
    }

    private void flush() {
        for (int i = 0; i < captureCount; i++) {
            capturePool.get(i).commit();
        }

        if (quadCount == 0) {
            return;
        }

        ensureOrderCapacity(quadCount);

        int sortedCount = 0;

        for (int quad = 0; quad < quadCount; quad++) {
            if (quadSorted[quad]) {
                order[sortedCount++] = quad;
            }
        }

        if (sortMode == SORT_CUSTOM) {
            sortCustom(sortedCount);
        } else {
            AzQuadOrdering.sortDescendingStable(order, quadKeys, sortedCount, scratch);
        }

        int total = sortedCount;

        for (int quad = 0; quad < quadCount; quad++) {
            if (!quadSorted[quad]) {
                order[total++] = quad;
            }
        }

        emit(total);
    }

    /**
     * A projection with a sorting we don't recognize: let it order the centroids, as vanilla would.
     */
    private void sortCustom(int sortedCount) {
        Vector3f[] centroids = new Vector3f[sortedCount];

        for (int i = 0; i < sortedCount; i++) {
            int base = order[i] * 3;
            centroids[i] = new Vector3f(quadCentroids[base], quadCentroids[base + 1], quadCentroids[base + 2]);
        }

        int[] permutation = customSorting.sort(centroids);
        System.arraycopy(order, 0, scratch, 0, sortedCount);

        for (int i = 0; i < sortedCount; i++) {
            order[i] = scratch[permutation[i]];
        }
    }

    private void emit(int count) {
        RenderType current = null;
        VertexConsumer consumer = null;

        for (int i = 0; i < count; i++) {
            int quad = order[i];
            RenderType renderType = capturePool.get(quadCapture[quad]).renderType;

            if (renderType != current) {
                // Switching render type ends the previous batch, so each run of one type becomes one draw.
                consumer = delegate.getBuffer(renderType);
                current = renderType;
            }

            int base = quad * INTS_PER_QUAD;

            for (int v = 0; v < 4; v++) {
                int o = base + v * INTS_PER_VERTEX;
                int color = vertices[o + 3];
                int uv1 = vertices[o + 6];
                int uv2 = vertices[o + 7];

                consumer.addVertex(
                    Float.intBitsToFloat(vertices[o]),
                    Float.intBitsToFloat(vertices[o + 1]),
                    Float.intBitsToFloat(vertices[o + 2])
                )
                    .setColor((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, color >>> 24)
                    .setUv(Float.intBitsToFloat(vertices[o + 4]), Float.intBitsToFloat(vertices[o + 5]))
                    .setUv1(uv1 & 0xFFFF, uv1 >>> 16)
                    .setUv2(uv2 & 0xFFFF, uv2 >>> 16)
                    .setNormal(
                        Float.intBitsToFloat(vertices[o + 8]),
                        Float.intBitsToFloat(vertices[o + 9]),
                        Float.intBitsToFloat(vertices[o + 10])
                    );
            }
        }
    }

    private void addQuad(Capture capture) {
        if (quadCount == quadCapture.length) {
            int size = quadCount * 2;
            vertices = Arrays.copyOf(vertices, size * INTS_PER_QUAD);
            quadCapture = Arrays.copyOf(quadCapture, size);
            quadSorted = Arrays.copyOf(quadSorted, size);
            quadKeys = Arrays.copyOf(quadKeys, size);
            quadCentroids = Arrays.copyOf(quadCentroids, size * 3);
        }

        int[] staging = capture.staging;
        System.arraycopy(staging, 0, vertices, quadCount * INTS_PER_QUAD, INTS_PER_QUAD);

        float cx = 0, cy = 0, cz = 0;

        for (int v = 0; v < 4; v++) {
            int o = v * INTS_PER_VERTEX;
            cx += Float.intBitsToFloat(staging[o]);
            cy += Float.intBitsToFloat(staging[o + 1]);
            cz += Float.intBitsToFloat(staging[o + 2]);
        }

        cx *= 0.25F;
        cy *= 0.25F;
        cz *= 0.25F;

        int centroidBase = quadCount * 3;
        quadCentroids[centroidBase] = cx;
        quadCentroids[centroidBase + 1] = cy;
        quadCentroids[centroidBase + 2] = cz;

        // Same keys as vanilla's VertexSorting: farthest from the camera first, or furthest back in a GUI.
        quadKeys[quadCount] = sortMode == SORT_ORTHOGRAPHIC ? -cz : cx * cx + cy * cy + cz * cz;
        quadCapture[quadCount] = capture.index;
        quadSorted[quadCount] = capture.sorted;
        quadCount++;
    }

    private void ensureOrderCapacity(int size) {
        if (order.length < size) {
            order = new int[Math.max(size, order.length * 2)];
            scratch = new int[order.length];
        }
    }

    private void reset() {
        for (int i = 0; i < captureCount; i++) {
            capturePool.get(i).clear();
        }

        sortedCaptures.clear();
        unsortedCaptures.clear();
        captureCount = 0;
        quadCount = 0;
        unsortedDepth = 0;
        // The delegate is kept, so anything still holding this sorter after the render just draws straight through.
        customSorting = null;
        active = false;
    }

    /**
     * Records the vertices written for one render type. Quads are staged here and committed to the sorter once
     * complete, so composite consumers that write to two captures at once (alternating calls) stay correct.
     */
    private final class Capture implements VertexConsumer {

        private final int[] staging = new int[INTS_PER_QUAD];

        private RenderType renderType;

        private boolean sorted;

        private int index;

        /** Vertices started in the staged quad, 0 to 4. */
        private int staged;

        void reset(RenderType renderType, boolean sorted, int index) {
            this.renderType = renderType;
            this.sorted = sorted;
            this.index = index;
            this.staged = 0;
        }

        void clear() {
            this.renderType = null;
            this.staged = 0;
        }

        /** Commits the staged quad if it is complete; an incomplete one is dropped. */
        void commit() {
            if (staged == 4) {
                addQuad(this);
            }

            staged = 0;
        }

        private int current() {
            return (staged - 1) * INTS_PER_VERTEX;
        }

        @Override
        public @NotNull VertexConsumer addVertex(float x, float y, float z) {
            if (staged == 4) {
                commit();
            }

            int o = staged * INTS_PER_VERTEX;
            staging[o] = Float.floatToRawIntBits(x);
            staging[o + 1] = Float.floatToRawIntBits(y);
            staging[o + 2] = Float.floatToRawIntBits(z);
            staging[o + 3] = -1;
            staging[o + 4] = 0;
            staging[o + 5] = 0;
            staging[o + 6] = 0;
            staging[o + 7] = 0;
            staging[o + 8] = 0;
            staging[o + 9] = Float.floatToRawIntBits(1);
            staging[o + 10] = 0;
            staged++;
            return this;
        }

        @Override
        public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
            staging[current() + 3] = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv(float u, float v) {
            int o = current();
            staging[o + 4] = Float.floatToRawIntBits(u);
            staging[o + 5] = Float.floatToRawIntBits(v);
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv1(int u, int v) {
            staging[current() + 6] = (u & 0xFFFF) | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public @NotNull VertexConsumer setUv2(int u, int v) {
            staging[current() + 7] = (u & 0xFFFF) | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public @NotNull VertexConsumer setNormal(float x, float y, float z) {
            int o = current();
            staging[o + 8] = Float.floatToRawIntBits(x);
            staging[o + 9] = Float.floatToRawIntBits(y);
            staging[o + 10] = Float.floatToRawIntBits(z);
            return this;
        }
    }
}
