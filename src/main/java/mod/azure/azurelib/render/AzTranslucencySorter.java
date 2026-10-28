package mod.azure.azurelib.render;

import mod.azure.azurelib.render.layer.AzRenderLayer;
import mod.azure.azurelib.render.vertex.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * Sorts all of one animatable's translucent geometry back to front, across every pass that draws it: the model, its
 * render layers, re-renders and bones with their own render type.
 * <p>
 * {@link AzBufferSource} records one batch per render type and draws the batches in the order they were first used,
 * without sorting anything. So translucent quads come out in submission order, and whichever translucent pass is drawn
 * first wins: it writes depth, and translucent geometry behind it (from the same pass or a later one) is hidden instead
 * of blended. This sorter sits between the pipeline and the real {@link MultiBufferSource} for the duration of one
 * render, captures the quads of every render type matching its filter instead of drawing them, then replays them all at
 * the end, farthest first.
 * <p>
 * Because {@link AzBufferSource} merges everything of one render type into a single batch, the replay ends the
 * delegate's batch before each run of a new render type, so each run is drawn in its sorted position. The first of
 * those also draws the solid and cutout geometry recorded earlier in the render, before any translucent geometry.
 * <p>
 * Distance is measured in eye space: captured positions are relative to the GL model-view matrix (the renderer's
 * {@code translate(x, y, z)} plus the camera), so the matrix current at replay time is read once and applied to each
 * quad's centroid. Perspective projections sort by distance from the eye, as vanilla does on later versions;
 * orthographic ones (GUI rendering) sort by depth.
 * <p>
 * Render types that don't match the filter (solid and cutout geometry, glints, outlines) go straight through to the
 * delegate.
 * <p>
 * Layers can opt out with {@link AzRenderLayer#sortsTranslucency()}: their translucent geometry is still held back, but
 * drawn in submission order after the sorted geometry. Overlays drawn on top of the same surfaces (glow masks, for
 * example) should opt out, since sorting coplanar quads from two passes interleaves them and costs a draw call per
 * switch.
 * <p>
 * Limits:
 * <ul>
 * <li>This orders one animatable's geometry. Separate entities still draw in the order the game renders them.</li>
 * <li>Geometry written straight to a {@link VertexConsumer} handed to the pipeline (items and armor pass their own,
 * with glint) bypasses the sorter; only what is requested from the buffer source is sorted.</li>
 * <li>The delegate's batches must still be drawn under the same GL model-view matrix as during this render, which is
 * already the contract of {@link AzBufferSource}.</li>
 * </ul>
 * One instance per pipeline, reused every render; capturing allocates nothing once its buffers have grown. Render
 * thread only.
 */
public final class AzTranslucencySorter implements MultiBufferSource {

    /**
     * The translucent, quad-based render types, excluding glint passes (which draw over the surface below them with an
     * equal depth test, so they must not be reordered away from it).
     */
    public static final Predicate<RenderType> DEFAULT_FILTER = renderType -> renderType.isTranslucent()
        && !renderType.isGlint()
        && renderType.glMode() == GL11.GL_QUADS;

    /** x, y, z, color, u, v, overlay, light, nx, ny, nz */
    private static final int INTS_PER_VERTEX = 11;

    private static final int INTS_PER_QUAD = INTS_PER_VERTEX * 4;

    private static final FloatBuffer MATRIX = BufferUtils.createFloatBuffer(16);

    private final float[] modelView = new float[16];

    private MultiBufferSource delegate;

    private Predicate<RenderType> filter = DEFAULT_FILTER;

    private boolean active;

    private int unsortedDepth;

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

    /**
     * Ends the batch of {@code source} if it is (or sorts into) an {@link AzBufferSource}. For code that has to draw
     * pending AzureLib geometry before handing over to vanilla rendering, and may be given this sorter as its buffer
     * source: captured translucent geometry stays held back, but everything already passed through is drawn.
     */
    public static void endBatch(MultiBufferSource source) {
        while (source instanceof AzTranslucencySorter) {
            source = ((AzTranslucencySorter) source).delegate;
        }

        if (source instanceof AzBufferSource) {
            ((AzBufferSource) source).endBatch();
        }
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (!active || !filter.test(renderType)) {
            return delegate.getBuffer(renderType);
        }

        boolean sorted = unsortedDepth == 0;
        IdentityHashMap<RenderType, Capture> captures = sorted ? sortedCaptures : unsortedCaptures;
        Capture capture = captures.get(renderType);

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
        if (quadCount == 0) {
            return;
        }

        computeKeys();
        ensureOrderCapacity(quadCount);

        int sortedCount = 0;

        for (int quad = 0; quad < quadCount; quad++) {
            if (quadSorted[quad]) {
                order[sortedCount++] = quad;
            }
        }

        AzQuadOrdering.sortDescendingStable(order, quadKeys, sortedCount, scratch);

        int total = sortedCount;

        for (int quad = 0; quad < quadCount; quad++) {
            if (!quadSorted[quad]) {
                order[total++] = quad;
            }
        }

        emit(total);
    }

    /**
     * Turns each sorted quad's centroid into a key that is larger the farther back the quad is, using the GL matrices
     * the delegate will draw with.
     */
    private void computeKeys() {
        MATRIX.clear();
        GlStateManager.getFloat(GL11.GL_PROJECTION_MATRIX, MATRIX);
        // Column-major: a perspective projection has -1 at [11] and 0 at [15]; an orthographic one has 1 at [15].
        boolean orthographic = MATRIX.get(15) == 1.0F;

        MATRIX.clear();
        GlStateManager.getFloat(GL11.GL_MODELVIEW_MATRIX, MATRIX);
        MATRIX.get(modelView);

        float[] m = modelView;

        for (int quad = 0; quad < quadCount; quad++) {
            if (!quadSorted[quad]) {
                continue;
            }

            int c = quad * 3;
            float x = quadCentroids[c];
            float y = quadCentroids[c + 1];
            float z = quadCentroids[c + 2];

            float eyeX = m[0] * x + m[4] * y + m[8] * z + m[12];
            float eyeY = m[1] * x + m[5] * y + m[9] * z + m[13];
            float eyeZ = m[2] * x + m[6] * y + m[10] * z + m[14];

            // The camera looks down -Z in eye space, so more negative Z is farther back.
            quadKeys[quad] = orthographic ? -eyeZ : eyeX * eyeX + eyeY * eyeY + eyeZ * eyeZ;
        }
    }

    private void emit(int count) {
        RenderType current = null;
        VertexConsumer consumer = null;

        for (int i = 0; i < count; i++) {
            int quad = order[i];
            RenderType renderType = capturePool.get(quadCapture[quad]).renderType;

            if (renderType != current) {
                // AzBufferSource merges all vertices of one render type into one batch, drawn in first-use order, so
                // end the batch here to draw everything before this run (including this render's solid geometry)
                // first. A nested sorter as the delegate orders the runs itself, so it is left alone.
                if (delegate instanceof AzBufferSource) {
                    ((AzBufferSource) delegate).endBatch();
                }

                consumer = delegate.getBuffer(renderType);
                current = renderType;
            }

            int base = quad * INTS_PER_QUAD;

            for (int v = 0; v < 4; v++) {
                int o = base + v * INTS_PER_VERTEX;
                int color = vertices[o + 3];
                int overlay = vertices[o + 6];
                int light = vertices[o + 7];

                consumer.vertex(
                    Float.intBitsToFloat(vertices[o]),
                    Float.intBitsToFloat(vertices[o + 1]),
                    Float.intBitsToFloat(vertices[o + 2])
                )
                    .color((color >>> 16) & 0xFF, (color >>> 8) & 0xFF, color & 0xFF, color >>> 24)
                    .uv(Float.intBitsToFloat(vertices[o + 4]), Float.intBitsToFloat(vertices[o + 5]))
                    .overlayCoords(overlay & 0xFFFF, overlay >>> 16)
                    .uv2(light & 0xFFFF, light >>> 16)
                    .normal(
                        Float.intBitsToFloat(vertices[o + 8]),
                        Float.intBitsToFloat(vertices[o + 9]),
                        Float.intBitsToFloat(vertices[o + 10])
                    );
                consumer.endVertex();
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

        int[] quadData = capture.quad;
        System.arraycopy(quadData, 0, vertices, quadCount * INTS_PER_QUAD, INTS_PER_QUAD);

        float cx = 0, cy = 0, cz = 0;

        for (int v = 0; v < 4; v++) {
            int o = v * INTS_PER_VERTEX;
            cx += Float.intBitsToFloat(quadData[o]);
            cy += Float.intBitsToFloat(quadData[o + 1]);
            cz += Float.intBitsToFloat(quadData[o + 2]);
        }

        int centroidBase = quadCount * 3;
        quadCentroids[centroidBase] = cx * 0.25F;
        quadCentroids[centroidBase + 1] = cy * 0.25F;
        quadCentroids[centroidBase + 2] = cz * 0.25F;

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
        active = false;
    }

    /**
     * Records the vertices written for one render type, mirroring {@link AzBufferSource}'s batch: elements are set one
     * by one and kept until changed (an element a vertex doesn't set reuses the previous vertex's value, starting from
     * the same defaults), and {@link #endVertex()} finishes the vertex. Every fourth vertex completes a quad, which is
     * committed to the sorter. Each capture keeps its own state, so composite consumers that write to two captures with
     * alternating calls stay correct.
     */
    private final class Capture implements VertexConsumer {

        /** The quad being built: 4 vertices of {@link #INTS_PER_VERTEX}. */
        private final int[] quad = new int[INTS_PER_QUAD];

        private RenderType renderType;

        private boolean sorted;

        private int index;

        /** Vertices finished in the current quad, 0 to 3. */
        private int finished;

        private float x, y, z, u, v, nx, ny, nz;

        private int color;

        private int overlay;

        private int light;

        void reset(RenderType renderType, boolean sorted, int index) {
            this.renderType = renderType;
            this.sorted = sorted;
            this.index = index;
            this.finished = 0;
            this.x = this.y = this.z = this.u = this.v = this.nx = this.ny = this.nz = 0;
            // AzBufferSource's batch defaults.
            this.color = 0xFFFFFFFF;
            this.overlay = OverlayTexture.NO_OVERLAY;
            this.light = LightTexture.FULL_BRIGHT;
        }

        void clear() {
            this.renderType = null;
            this.finished = 0;
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.x = (float) x;
            this.y = (float) y;
            this.z = (float) z;
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            this.color = (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            this.u = u;
            this.v = v;
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.overlay = u & 0xFFFF | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            this.light = u & 0xFFFF | (v & 0xFFFF) << 16;
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.nx = x;
            this.ny = y;
            this.nz = z;
            return this;
        }

        @Override
        public void endVertex() {
            int o = finished * INTS_PER_VERTEX;
            quad[o] = Float.floatToRawIntBits(x);
            quad[o + 1] = Float.floatToRawIntBits(y);
            quad[o + 2] = Float.floatToRawIntBits(z);
            quad[o + 3] = color;
            quad[o + 4] = Float.floatToRawIntBits(u);
            quad[o + 5] = Float.floatToRawIntBits(v);
            quad[o + 6] = overlay;
            quad[o + 7] = light;
            quad[o + 8] = Float.floatToRawIntBits(nx);
            quad[o + 9] = Float.floatToRawIntBits(ny);
            quad[o + 10] = Float.floatToRawIntBits(nz);

            if (++finished == 4) {
                addQuad(this);
                finished = 0;
            }
        }
    }
}
