package mod.azure.azurelib.render.entity;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.render.AzBufferSource;

/**
 * Per-frame cache of baked, entity-local vertex data keyed by the animation/pose state that produced it. Lets
 * {@link AzEntityRenderer} skip re-walking the bone tree for entities that share an identical pose this frame (e.g. a
 * swarm of identical mobs), replaying the recorded geometry translated to each entity's camera-relative position
 * instead.
 */
public final class AzEntityGeometryCache {

    static final VertexSnapshot UNCACHEABLE = new VertexSnapshot(new float[0], 0, null, 0, 0, 0);

    private static final int MAX_CACHE_SIZE = 4096;

    private static final Logger LOGGER = LoggerFactory.getLogger(AzEntityGeometryCache.class);

    private static final Map<CacheKey, VertexSnapshot> CACHE = new Object2ObjectOpenHashMap<>();

    private static long lastClearTick = Long.MIN_VALUE;

    private static int lastClearPartialBits = Integer.MIN_VALUE;

    private static boolean overflowLogged = false;

    private static final Map<EntityType<?>, Gate> GATES = new Reference2ObjectOpenHashMap<>();

    private static long frame;

    private static final class Gate {

        boolean enabled = true;

        int framesWithoutHit;

        long nextProbeFrame;

        boolean hitThisFrame;

        void endFrame() {
            if (hitThisFrame) {
                hitThisFrame = false;
                framesWithoutHit = 0;
                enabled = true;
                return;
            }

            if (enabled && ++framesWithoutHit >= 20) {
                enabled = false;
                nextProbeFrame = frame + 200;
            } else if (!enabled && frame >= nextProbeFrame) {
                enabled = true;
                framesWithoutHit = 20 - 2;
            }
        }
    }

    private AzEntityGeometryCache() {}

    /**
     * Clears the cache once per (game tick, partial tick) pair. Must be called before the first lookup each frame.
     */
    public static void maybeReset(long gameTick, float partialTick) {
        int ptBits = Float.floatToRawIntBits(partialTick);
        if (gameTick != lastClearTick || ptBits != lastClearPartialBits) {
            CACHE.clear();
            lastClearTick = gameTick;
            lastClearPartialBits = ptBits;
            frame++;

            for (var gate : GATES.values()) {
                gate.endFrame();
            }
        }
    }

    /**
     * Whether entities of {@code type} should use the cache this frame. When this returns {@code false}, skip hashing,
     * lookup and capture entirely.
     */
    public static boolean isEnabledFor(EntityType<?> type) {
        var gate = GATES.computeIfAbsent(type, k -> new Gate());

        return gate.enabled;
    }

    /** Records a cache hit for {@code type}, keeping its caching enabled. */
    public static void recordHit(EntityType<?> type) {
        var gate = GATES.get(type);

        if (gate != null) {
            gate.hitThisFrame = true;
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
                    "AzEntityGeometryCache exceeded {} entries without a frame reset (maybeReset "
                        + "did not fire) — forcing a clear. This indicates entries are leaking "
                        + "across frames; report this if it recurs.",
                    MAX_CACHE_SIZE
                );
            }
            CACHE.clear();
        }
        CACHE.put(key, snapshot);
    }

    /**
     * Marks a pose as unsuitable for caching (e.g. because it depends on more than the modelled state, such as live
     * procedural input). Callers should skip both {@link #get} and {@link #put} for such poses.
     */
    public static boolean isUncacheable(VertexSnapshot snapshot) {
        return snapshot == UNCACHEABLE;
    }

    public static VertexSnapshot uncacheable() {
        return UNCACHEABLE;
    }

    /**
     * Snapshots are entity-local (extraction runs against an identity pose), so the key holds only what shapes the
     * geometry: the model, the animated bone state and the entity's own pose transform. It used to include the entity's
     * position as well, which made every entity's key unique and the cache unable to hit.
     */
    public record CacheKey(
        UUID modelUUID,
        EntityType<?> entityType,
        long boneStateHash,
        long poseStateHash,
        RenderType renderType
    ) {}

    public record VertexSnapshot(
        float[] data,
        int vertexCount,
        RenderType renderType,
        float sourceCamX,
        float sourceCamY,
        float sourceCamZ
    ) {

        /**
         * Replays this snapshot's vertices into {@code bufferSource}, offsetting each position by the delta between the
         * camera position the snapshot was baked with and {@code targetCam...}, and stamping the given light, overlay,
         * and color onto every vertex.
         */
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
