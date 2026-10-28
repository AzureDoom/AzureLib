package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import mod.azure.azurelib.render.layer.AzRenderLayer;

/**
 * Sorts all of one animatable's translucent geometry back to front, across every pass that draws it: the model, its
 * render layers, re-renders and bones with their own render type.
 * <p>
 * Vanilla sorts the quads within each translucent render type, but different render types are separate batches, and
 * within one submit order they draw in a fixed sequence regardless of depth. So when an animatable draws translucent
 * geometry with more than one render type (a translucent shell layer over a translucent body, say), whichever batch is
 * drawn first wins: it writes depth, and translucent geometry of the other type behind it is hidden instead of blended.
 * <p>
 * On 26.x nothing is drawn while the pipeline walks the model: {@link AzBufferSource} records every vertex per render
 * type, entity-local, and submits the recordings later. Sorting therefore happens at submit time, here:
 * {@link AzBufferSource} hands over the recordings of every render type matching the renderer's filter, and this sorts
 * all of their quads together by distance from the camera (centroids transformed by the submit pose, which is
 * camera-relative) and submits them in runs, one run per stretch of quads sharing a render type. Each run goes to its
 * own {@link SubmitNodeCollector#order(int) submit order}, starting at {@link #SORTED_ORDER_BASE}, because orders are
 * drawn one after another while render types within one order are not drawn in submission order.
 * <p>
 * When the sorted geometry uses a single render type there is nothing to reorder across batches, and vanilla already
 * sorts within the batch, so it is submitted as one batch on the current order, exactly as without sorting.
 * <p>
 * Layers can opt out with {@link AzRenderLayer#sortsTranslucency()}: their translucent geometry is recorded separately
 * and submitted after the sorted geometry (on the order after the last run), together with mirrored geometry such as
 * the auto-glowing layer's.
 * <p>
 * Limits:
 * <ul>
 * <li>This orders one animatable's geometry. Separate entities still draw in the order the game renders them; when runs
 * are spread over orders, this animatable's translucent geometry also draws after other entities' order-0
 * geometry.</li>
 * <li>It relies on the feature dispatcher drawing each submit order before the next. If a version batches a render type
 * across orders, runs of the same type merge again and ordering falls back to vanilla's per-render-type sorting.</li>
 * <li>Keys use distance from the camera, so GUI (orthographic) rendering is sorted only approximately.</li>
 * </ul>
 * Render thread only; the scratch arrays are shared, while each run's quad list comes from {@link AzFrameArena} so it
 * stays valid until the submitted geometry is drawn.
 */
public final class AzTranslucencySorter {

    /**
     * Quad-based render types that vanilla sorts on upload, i.e. the translucent ones.
     */
    public static final Predicate<RenderType> DEFAULT_FILTER = renderType -> renderType.sortOnUpload()
        && renderType.primitiveTopology() == PrimitiveTopology.QUADS;

    /** The submit order of the first sorted run; vanilla features use order 0 by default. */
    public static final int SORTED_ORDER_BASE = 1;

    private static final Vector3f CENTROID = new Vector3f();

    private static int[] quadType = new int[256];

    private static int[] quadIndex = new int[256];

    private static float[] keys = new float[256];

    private static int[] order = new int[256];

    private static int[] scratch = new int[256];

    private AzTranslucencySorter() {}

    /**
     * Submits the given recordings back to front.
     *
     * @param collector  the collector the buffer source is being submitted to
     * @param poseStack  the submit pose; recordings are entity-local and replayed against it
     * @param types      the sorted render types
     * @param recordings their recordings, in the same order
     * @return the collector to submit anything that must draw after the sorted geometry to
     */
    static OrderedSubmitNodeCollector submitSorted(
        SubmitNodeCollector collector,
        PoseStack poseStack,
        List<RenderType> types,
        List<AzBufferSource.Recording> recordings
    ) {
        if (types.size() == 1) {
            // One batch: vanilla sorts its quads, and there is nothing to interleave it with.
            collector.submitCustomGeometry(poseStack, types.getFirst(), recordings.getFirst()::replay);
            return collector;
        }

        var total = 0;

        for (int t = 0; t < types.size(); t++) {
            var recording = recordings.get(t);

            if ((recording.vertexCount() & 3) != 0) {
                // Not whole quads, so it can't be split up: submit it as it is.
                collector.submitCustomGeometry(poseStack, types.get(t), recording::replay);
                continue;
            }

            total += recording.quadCount();
        }

        ensureCapacity(total);

        var matrix = poseStack.last().pose();
        var count = 0;

        for (int t = 0; t < types.size(); t++) {
            var recording = recordings.get(t);

            if ((recording.vertexCount() & 3) != 0) {
                continue;
            }

            for (int quad = 0, quads = recording.quadCount(); quad < quads; quad++) {
                recording.quadCentroid(quad, CENTROID);
                matrix.transformPosition(CENTROID);

                quadType[count] = t;
                quadIndex[count] = quad;
                // Camera-relative, so the length is the distance from the camera; vanilla sorts by the same key.
                keys[count] = CENTROID.lengthSquared();
                order[count] = count;
                count++;
            }
        }

        AzQuadOrdering.sortDescendingStable(order, keys, count, scratch);

        var run = 0;
        var start = 0;

        while (start < count) {
            var type = quadType[order[start]];
            var end = start + 1;

            while (end < count && quadType[order[end]] == type) {
                end++;
            }

            var length = end - start;
            // Frame-lifetime storage: the callback runs later, when the dispatcher draws this order.
            var quads = AzFrameArena.ints(length);

            for (int i = 0; i < length; i++) {
                quads[i] = quadIndex[order[start + i]];
            }

            var recording = recordings.get(type);
            collector.order(SORTED_ORDER_BASE + run)
                .submitCustomGeometry(
                    poseStack,
                    types.get(type),
                    (pose, consumer) -> recording.replayQuads(pose, consumer, quads, length)
                );

            run++;
            start = end;
        }

        return run == 0 ? collector : collector.order(SORTED_ORDER_BASE + run);
    }

    private static void ensureCapacity(int size) {
        if (quadType.length < size) {
            var length = Math.max(size, quadType.length * 2);
            quadType = Arrays.copyOf(quadType, length);
            quadIndex = Arrays.copyOf(quadIndex, length);
            keys = Arrays.copyOf(keys, length);
            order = new int[length];
            scratch = new int[length];
        }
    }
}
