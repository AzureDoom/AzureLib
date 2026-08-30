package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 */
public final class AzBufferSource {

    private final Map<RenderType, RecordingConsumer> buffers = new LinkedHashMap<>();

    private final List<DeferredEntry> deferred = new ArrayList<>();

    public VertexConsumer getBuffer(RenderType renderType) {
        return buffers.computeIfAbsent(renderType, type -> new RecordingConsumer());
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

    public void submitAll(SubmitNodeCollector collector, PoseStack poseStack) {
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
            return;
        }

        for (var entry : buffers.entrySet()) {
            var recording = entry.getValue();

            if (recording.isEmpty()) {
                continue;
            }

            var snapshot = recording.snapshot();
            collector.submitCustomGeometry(poseStack, entry.getKey(), snapshot::replay);
        }

        buffers.clear();
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
            return;
        }

        for (var entry : buffers.entrySet()) {
            var recording = entry.getValue();

            if (recording.isEmpty()) {
                continue;
            }

            var snapshot = recording.snapshot();
            collector.submitCustomGeometry(poseStack, entry.getKey(), snapshot::replay);
        }

        buffers.clear();
    }

    private record Recording(
        float[] floats,
        int[] ints,
        int vertexCount
    ) {

        private void replay(PoseStack.Pose pose, VertexConsumer consumer) {
            var matrix = pose.pose();
            var position = new Vector3f();
            var normal = new Vector3f();

            for (var i = 0; i < vertexCount; i++) {
                var f = i * RecordingConsumer.FLOAT_STRIDE;
                var n = i * RecordingConsumer.INT_STRIDE;

                matrix.transformPosition(floats[f], floats[f + 1], floats[f + 2], position);
                pose.transformNormal(floats[f + 5], floats[f + 6], floats[f + 7], normal);

                consumer.addVertex(position.x, position.y, position.z)
                    .setColor(ints[n])
                    .setUv(floats[f + 3], floats[f + 4])
                    .setOverlay(ints[n + 1])
                    .setLight(ints[n + 2])
                    .setNormal(normal.x, normal.y, normal.z);
            }
        }
    }

    /**
     * A {@link VertexConsumer} with no delegate that accumulates the full vertex format AzureLib emits
     * ({@code position, color, uv, overlay, light, normal}). Vertices are flushed on {@code setNormal}, the last call
     * of the builder chain.
     */
    private static final class RecordingConsumer implements VertexConsumer {

        private static final int FLOAT_STRIDE = 8;

        private static final int INT_STRIDE = 3;

        private static final int INITIAL_VERTICES = 512;

        private float[] floats = new float[INITIAL_VERTICES * FLOAT_STRIDE];

        private int[] ints = new int[INITIAL_VERTICES * INT_STRIDE];

        private int vertexCount;

        private float vX, vY, vZ;

        private float vU, vV;

        private int vColor = 0xFFFFFFFF;

        private int vOverlay;

        private int vLight;

        private boolean isEmpty() {
            return vertexCount == 0;
        }

        private Recording snapshot() {
            var recording = new Recording(
                Arrays.copyOf(floats, vertexCount * FLOAT_STRIDE),
                Arrays.copyOf(ints, vertexCount * INT_STRIDE),
                vertexCount
            );
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
            if ((vertexCount + 1) * FLOAT_STRIDE > floats.length) {
                floats = Arrays.copyOf(floats, floats.length * 2);
                ints = Arrays.copyOf(ints, ints.length * 2);
            }
        }
    }
}
