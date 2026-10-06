package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 26.2 stand-in for {@code MultiBufferSource}.
 * <p>
 * 26.2 removed direct buffer acquisition in favor of a deferred submit model: geometry is handed to a
 * {@link SubmitNodeCollector} as a callback that is invoked later, during the render pass, with a
 * {@code VertexConsumer} the engine owns. AzureLib's pipeline is built the other way around, it asks for a consumer up
 * front and writes into it as it walks the bone tree.
 * <p>
 * This class bridges the two: {@link #getBuffer(RenderType)} hands out a recording consumer that stores every vertex
 * verbatim, keyed by {@code RenderType}, and {@link #submitAll} replays each recorded batch through
 * {@code submitCustomGeometry}. AzureLib writes fully world-transformed positions (see
 * {@code AzModelRenderer#createVerticesOfQuad}, which multiplies by the pose matrix itself), so replay happens against
 * the pose supplied at submit time; the pipeline itself runs against an identity {@link PoseStack} during extraction,
 * so the recorded vertices are entity-local.
 * <p>
 * A buffer source lives for one render of one animatable, so recordings are sized up front from what the same render
 * type needed last time and handed to the submit callback without copying. {@link #mirror} lets a layer re-submit a
 * range of an existing recording under another render type, instead of walking the model a second time.
 */
@SuppressWarnings("unused")
public final class AzBufferSource {

    /** Sentinel for {@link #mirror}'s {@code light} parameter: keep each vertex's recorded light. */
    public static final int KEEP_LIGHT = -1;

    /** {@link #mirror}'s {@code tint} for no tint. */
    public static final int NO_TINT = 0xFFFFFFFF;

    /**
     * Vertex count each render type needed the last time it was recorded, so a new recording can be allocated at the
     * right size instead of growing (and copying) its way there. Render types are memoized by the game, so this stays
     * small.
     */
    private static final Map<RenderType, Integer> CAPACITY_HINTS = new ConcurrentHashMap<>();

    private static final Function<RenderType, RecordingConsumer> NEW_CONSUMER = type -> new RecordingConsumer(
        initialCapacity(type)
    );

    private final Map<RenderType, RecordingConsumer> buffers = new LinkedHashMap<>();

    private final List<DeferredEntry> deferred = new ArrayList<>();

    private final List<MirrorEntry> mirrors = new ArrayList<>();

    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, NEW_CONSUMER);
    }

    /** Number of vertices recorded so far for {@code renderType}, or 0 if nothing has been recorded for it. */
    public int vertexCount(RenderType renderType) {
        var recording = renderType == null ? null : buffers.get(renderType);
        return recording == null ? 0 : recording.vertexCount;
    }

    /** Number of vertices recorded so far across every render type. */
    public int totalVertexCount() {
        var total = 0;

        for (var recording : buffers.values()) {
            total += recording.vertexCount;
        }

        return total;
    }

    /**
     * Re-submits vertices {@code [startVertex, endVertex)} of the {@code source} recording under {@code target} when
     * this buffer source is submitted, with the light replaced and the color multiplied by {@code tint}. Positions,
     * UVs, normals and overlay are reused as recorded.
     * <p>
     * This is how a layer like the auto-glowing layer draws the model a second time with another texture without
     * re-walking the bone tree.
     *
     * @param light the packed light to stamp on every vertex, or {@link #KEEP_LIGHT}
     * @param tint  ARGB multiplier for the recorded color, or {@link #NO_TINT}
     */
    public void mirror(RenderType source, int startVertex, int endVertex, RenderType target, int light, int tint) {
        mirror(source, startVertex, endVertex, target, light, tint, null);
    }

    /**
     * Picks which quads of a recording to keep, from the bounds of each quad's UVs (normalized texture coordinates).
     */
    @FunctionalInterface
    public interface QuadFilter {

        boolean test(float minU, float minV, float maxU, float maxV);
    }

    /**
     * {@link #mirror(RenderType, int, int, RenderType, int, int)} that only re-submits the quads {@code filter} keeps,
     * e.g. only the quads whose UVs touch glowing pixels of a glowmask. The range must hold whole quads (four vertices
     * each, as the model pass writes them); otherwise the filter is ignored and the whole range is mirrored.
     *
     * @param filter which quads to keep, or {@code null} for all of them
     */
    public void mirror(
        RenderType source,
        int startVertex,
        int endVertex,
        RenderType target,
        int light,
        int tint,
        @Nullable QuadFilter filter
    ) {
        if (endVertex <= startVertex) {
            return;
        }

        int[] runs = null;
        var consumer = filter == null ? null : buffers.get(source);

        if (
            consumer != null && ((endVertex - startVertex) & 3) == 0 && endVertex <= consumer.vertexCount
        ) {
            runs = consumer.filterQuads(startVertex, endVertex, filter);

            if (runs.length == 0) {
                return;
            }

            if (runs.length == 2 && runs[0] == startVertex && runs[1] == endVertex) {
                runs = null;
            }
        }

        mirrors.add(new MirrorEntry(source, startVertex, endVertex, target, light, tint, runs));
    }

    @FunctionalInterface
    public interface DeferredSubmit {

        void submit(PoseStack poseStack, SubmitNodeCollector collector);
    }

    public void defer(PoseStack current, DeferredSubmit action) {
        deferred.add(new DeferredEntry(current.last().copy(), action));
    }

    private record DeferredEntry(
        PoseStack.Pose pose,
        DeferredSubmit action
    ) {}

    /**
     * @param runs vertex ranges to replay as {@code [start0, end0, start1, end1, ...]}, or {@code null} for the whole
     *             of {@code [startVertex, endVertex)}
     */
    private record MirrorEntry(
        RenderType source,
        int startVertex,
        int endVertex,
        RenderType target,
        int light,
        int tint,
        int[] runs
    ) {}

    public void submitAll(SubmitNodeCollector collector, PoseStack poseStack) {
        submitInto(collector, poseStack);
    }

    public boolean isEmpty() {
        return buffers.isEmpty() && deferred.isEmpty();
    }

    /**
     * Immediately replays every recorded batch into a real, already-obtained {@code MultiBufferSource}, without going
     * through a {@link SubmitNodeCollector}. For call sites that are still purely immediate-mode and never see a submit
     * phase at all (e.g. armor rendering via the vanilla {@code HumanoidModel#renderToBuffer} hook) — as opposed to
     * {@link #submitAll}, which defers replay to the engine's later submit pass.
     */
    public void flushInto(SubmitNodeCollector collector, PoseStack poseStack) {
        submitInto(collector, poseStack);
    }

    private void submitInto(SubmitNodeCollector collector, PoseStack poseStack) {
        if (!deferred.isEmpty()) {
            for (var entry : deferred) {
                poseStack.pushPose();
                poseStack.last().mulPose(entry.pose().pose());
                entry.action().submit(poseStack, collector);
                poseStack.popPose();
            }

            deferred.clear();
        }

        if (buffers.isEmpty()) {
            mirrors.clear();
            return;
        }

        Map<RenderType, Recording> recordings = mirrors.isEmpty() ? null : new LinkedHashMap<>();

        for (var entry : buffers.entrySet()) {
            var consumer = entry.getValue();

            if (consumer.isEmpty()) {
                continue;
            }

            var renderType = entry.getKey();
            var recording = consumer.snapshot();
            updateCapacityHint(renderType, recording.vertexCount());
            collector.submitCustomGeometry(poseStack, renderType, recording::replay);

            if (recordings != null) {
                recordings.put(renderType, recording);
            }
        }

        if (recordings != null) {
            for (var mirror : mirrors) {
                var recording = recordings.get(mirror.source());

                if (recording == null) {
                    continue;
                }

                var end = Math.min(mirror.endVertex(), recording.vertexCount());

                if (mirror.startVertex() >= end) {
                    continue;
                }

                var runs = mirror.runs() != null ? mirror.runs() : new int[] { mirror.startVertex(), end };

                collector.submitCustomGeometry(
                    poseStack,
                    mirror.target(),
                    (pose, consumer) -> {
                        for (int i = 0; i < runs.length; i += 2) {
                            recording.replay(
                                pose,
                                consumer,
                                runs[i],
                                Math.min(runs[i + 1], end),
                                mirror.light(),
                                mirror.tint()
                            );
                        }
                    }
                );
            }

            mirrors.clear();
        }

        buffers.clear();
    }

    private static int initialCapacity(RenderType renderType) {
        var hint = renderType == null ? null : CAPACITY_HINTS.get(renderType);

        return hint == null ? RecordingConsumer.INITIAL_VERTICES : hint + (hint >> 3);
    }

    private static void updateCapacityHint(RenderType renderType, int vertexCount) {
        if (renderType == null) {
            return;
        }

        var hint = CAPACITY_HINTS.get(renderType);

        if (hint == null || vertexCount > hint || vertexCount < hint / 2) {
            CAPACITY_HINTS.put(renderType, Math.max(vertexCount, 16));
        }
    }

    private static int multiplyColors(int a, int b) {
        var alpha = ((a >>> 24) * (b >>> 24) + 127) / 255;
        var red = (((a >> 16) & 0xFF) * ((b >> 16) & 0xFF) + 127) / 255;
        var green = (((a >> 8) & 0xFF) * ((b >> 8) & 0xFF) + 127) / 255;
        var blue = ((a & 0xFF) * (b & 0xFF) + 127) / 255;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private record Recording(
        float[] floats,
        int[] ints,
        int vertexCount
    ) {

        private void replay(PoseStack.Pose pose, VertexConsumer consumer) {
            replay(pose, consumer, 0, vertexCount, KEEP_LIGHT, NO_TINT);
        }

        private void replay(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            int startVertex,
            int endVertex,
            int lightOverride,
            int tint
        ) {
            var matrix = pose.pose();
            var position = new Vector3f();
            var normal = new Vector3f();

            for (var i = startVertex; i < endVertex; i++) {
                var f = i * RecordingConsumer.FLOAT_STRIDE;
                var n = i * RecordingConsumer.INT_STRIDE;

                matrix.transformPosition(floats[f], floats[f + 1], floats[f + 2], position);
                pose.transformNormal(floats[f + 5], floats[f + 6], floats[f + 7], normal);

                var color = tint == NO_TINT ? ints[n] : multiplyColors(ints[n], tint);
                var light = lightOverride == KEEP_LIGHT ? ints[n + 2] : lightOverride;

                consumer.addVertex(position.x, position.y, position.z)
                    .setColor(color)
                    .setUv(floats[f + 3], floats[f + 4])
                    .setOverlay(ints[n + 1])
                    .setLight(light)
                    .setNormal(normal.x, normal.y, normal.z);
            }
        }
    }

    private static final class RecordingConsumer implements VertexConsumer {

        private static final int FLOAT_STRIDE = 8;

        private static final int INT_STRIDE = 3;

        private static final int INITIAL_VERTICES = 512;

        private float[] floats;

        private int[] ints;

        private int vertexCount;

        private float vX, vY, vZ;

        private float vU, vV;

        private int vColor = 0xFFFFFFFF;

        private int vOverlay;

        private int vLight;

        private RecordingConsumer(int initialVertices) {
            this.floats = AzFrameArena.floats(initialVertices * FLOAT_STRIDE);
            this.ints = AzFrameArena.ints(initialVertices * INT_STRIDE);
        }

        private boolean isEmpty() {
            return vertexCount == 0;
        }

        /**
         * Tests each quad in {@code [start, end)} against {@code filter} by the bounds of its four UVs, and returns the
         * kept quads as merged vertex runs {@code [start0, end0, start1, end1, ...]}.
         */
        private int[] filterQuads(int start, int end, QuadFilter filter) {
            var runs = new int[16];
            var count = 0;
            var runStart = -1;

            for (int quad = start; quad < end; quad += 4) {
                var f = quad * FLOAT_STRIDE + 3;
                float minU = floats[f], maxU = minU, minV = floats[f + 1], maxV = minV;

                for (int v = 1; v < 4; v++) {
                    var u = floats[f + v * FLOAT_STRIDE];
                    var w = floats[f + v * FLOAT_STRIDE + 1];
                    minU = Math.min(minU, u);
                    maxU = Math.max(maxU, u);
                    minV = Math.min(minV, w);
                    maxV = Math.max(maxV, w);
                }

                if (filter.test(minU, minV, maxU, maxV)) {
                    if (runStart < 0) {
                        runStart = quad;
                    }
                } else if (runStart >= 0) {
                    if (count + 2 > runs.length) {
                        runs = Arrays.copyOf(runs, runs.length * 2);
                    }

                    runs[count++] = runStart;
                    runs[count++] = quad;
                    runStart = -1;
                }
            }

            if (runStart >= 0) {
                if (count + 2 > runs.length) {
                    runs = Arrays.copyOf(runs, runs.length * 2);
                }

                runs[count++] = runStart;
                runs[count++] = end;
            }

            return count == runs.length ? runs : Arrays.copyOf(runs, count);
        }

        /**
         * Hands the recorded arrays to an immutable {@link Recording} without copying. Each buffer source is used for
         * one render and cleared after submitting, so nothing writes to these arrays afterwards; the consumer is left
         * empty in case it is written to again.
         */
        private Recording snapshot() {
            var recording = new Recording(floats, ints, vertexCount);
            floats = AzFrameArena.floats(0);
            ints = AzFrameArena.ints(0);
            vertexCount = 0;
            return recording;
        }

        @Override
        public @NonNull VertexConsumer addVertex(float x, float y, float z) {
            vX = x;
            vY = y;
            vZ = z;
            return this;
        }

        @Override
        public @NonNull VertexConsumer setColor(int r, int g, int b, int a) {
            vColor = (a << 24) | (r << 16) | (g << 8) | b;
            return this;
        }

        @Override
        public @NonNull VertexConsumer setColor(int argb) {
            vColor = argb;
            return this;
        }

        @Override
        public @NonNull VertexConsumer setUv(float u, float v) {
            vU = u;
            vV = v;
            return this;
        }

        @Override
        public @NonNull VertexConsumer setUv1(int u, int v) {
            vOverlay = (v << 16) | (u & 0xFFFF);
            return this;
        }

        @Override
        public @NonNull VertexConsumer setUv2(int u, int v) {
            vLight = (v << 16) | (u & 0xFFFF);
            return this;
        }

        @Override
        public @NonNull VertexConsumer setUv3(float v, float v1) {
            return this;
        }

        @Override
        public @NonNull VertexConsumer setNormal(float nx, float ny, float nz) {
            ensureCapacity();

            var f = vertexCount * FLOAT_STRIDE;
            var n = vertexCount * INT_STRIDE;

            floats[f] = vX;
            floats[f + 1] = vY;
            floats[f + 2] = vZ;
            floats[f + 3] = vU;
            floats[f + 4] = vV;
            floats[f + 5] = nx;
            floats[f + 6] = ny;
            floats[f + 7] = nz;

            ints[n] = vColor;
            ints[n + 1] = vOverlay;
            ints[n + 2] = vLight;

            vertexCount++;
            return this;
        }

        @Override
        public @NonNull VertexConsumer setLineWidth(float width) {
            return this;
        }

        private void ensureCapacity() {
            if ((vertexCount + 1) * FLOAT_STRIDE > floats.length || (vertexCount + 1) * INT_STRIDE > ints.length) {
                var capacity = Math.min(floats.length / FLOAT_STRIDE, ints.length / INT_STRIDE);
                var vertices = Math.max(INITIAL_VERTICES, capacity * 2);
                var newFloats = AzFrameArena.floats(vertices * FLOAT_STRIDE);
                var newInts = AzFrameArena.ints(vertices * INT_STRIDE);
                System.arraycopy(floats, 0, newFloats, 0, vertexCount * FLOAT_STRIDE);
                System.arraycopy(ints, 0, newInts, 0, vertexCount * INT_STRIDE);
                floats = newFloats;
                ints = newInts;
            }
        }
    }
}
