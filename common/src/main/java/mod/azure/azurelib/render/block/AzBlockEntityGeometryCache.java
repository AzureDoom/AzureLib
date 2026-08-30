package mod.azure.azurelib.render.block;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Per-frame cache of baked, block-local vertex data keyed by the state that produced it. See
 * {@code AzEntityGeometryCache} for the rationale; this is the block entity equivalent.
 */
public final class AzBlockEntityGeometryCache {

    static final VertexSnapshot UNCACHEABLE = new VertexSnapshot(new float[0], 0, null, 0, 0, 0);

    private static final int MAX_CACHE_SIZE = 4096;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzBlockEntityGeometryCache.class);

    private static final Map<CacheKey, VertexSnapshot> CACHE = new Object2ObjectOpenHashMap<>();

    private static long lastClearTick = Long.MIN_VALUE;

    private static int lastClearPartialBits = Integer.MIN_VALUE;

    private static boolean overflowLogged = false;

    private AzBlockEntityGeometryCache() {}

    public static void maybeReset(long gameTick, float partialTick) {
        int ptBits = Float.floatToRawIntBits(partialTick);
        if (gameTick != lastClearTick || ptBits != lastClearPartialBits) {
            CACHE.clear();
            lastClearTick = gameTick;
            lastClearPartialBits = ptBits;
        }
    }

    public static VertexSnapshot get(CacheKey key) {
        return CACHE.get(key);
    }

    public static void put(CacheKey key, VertexSnapshot snapshot) {
        if (CACHE.size() >= MAX_CACHE_SIZE) {
            if (!overflowLogged) {
                overflowLogged = true;
                LOGGER.warn(
                    "AzBlockEntityGeometryCache exceeded {} entries without a frame reset "
                        + "(maybeReset did not fire) — forcing a clear. This indicates entries are "
                        + "leaking across frames; report this if it recurs.",
                    MAX_CACHE_SIZE
                );
            }
            CACHE.clear();
        }
        CACHE.put(key, snapshot);
    }

    public static boolean isUncacheable(VertexSnapshot snapshot) {
        return snapshot == UNCACHEABLE;
    }

    public static VertexSnapshot uncacheable() {
        return UNCACHEABLE;
    }

    public record CacheKey(
        UUID modelUUID,
        BlockState blockState,
        long boneStateHash,
        RenderType renderType,
        int passCamXBits,
        int passCamYBits,
        int passCamZBits
    ) {}

    public record VertexSnapshot(
        float[] data,
        int vertexCount,
        RenderType renderType,
        float sourceCamX,
        float sourceCamY,
        float sourceCamZ
    ) {

        public void replayTo(
            AzBufferSource bufferSource,
            float targetCamX,
            float targetCamY,
            float targetCamZ,
            int packedLight,
            int packedOverlay,
            int color
        ) {
            float dx = targetCamX - sourceCamX;
            float dy = targetCamY - sourceCamY;
            float dz = targetCamZ - sourceCamZ;

            var buffer = bufferSource.getBuffer(renderType);
            float[] d = data;
            int len = vertexCount * 8;
            for (int i = 0; i < len; i += 8) {
                buffer.addVertex(d[i] + dx, d[i + 1] + dy, d[i + 2] + dz)
                    .setColor(color)
                    .setUv(d[i + 3], d[i + 4])
                    .setOverlay(packedOverlay)
                    .setLight(packedLight)
                    .setNormal(d[i + 5], d[i + 6], d[i + 7]);
            }
        }
    }
}
