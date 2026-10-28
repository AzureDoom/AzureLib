package mod.azure.azurelib.hitbox;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public final class AzHitboxState {

    static final int FLOATS_PER_BOX = 10;

    private final AzHitboxSet set;

    private final int count;

    private final float[] pose;

    private final boolean[] active;

    private float referenceYaw;

    private long poseTime = Long.MIN_VALUE;

    private int version;

    private final AzOrientedBox[] worldCache;

    private int cacheVersion = -1;

    private double cacheX, cacheY, cacheZ;

    private float cacheYaw;

    @Nullable
    private AABB cacheBounds;

    @Nullable
    UUID fitModel;

    @Nullable
    float[][] fitCache;

    long lastReportTime = Long.MIN_VALUE;

    long lastRenderCaptureTime = Long.MIN_VALUE;

    @Nullable
    UUID reporter;

    double reporterDistanceSqr;

    long reporterTime = Long.MIN_VALUE;

    @Nullable
    AzHitboxHit lastHit;

    long lastHitTime = Long.MIN_VALUE;

    @Nullable
    AzHitboxHit pendingHit;

    @Nullable
    DamageSource pendingSource;

    private long cacheEnabledMask;

    AzHitboxState(AzHitboxSet set) {
        this.set = set;
        this.count = set.size();
        this.pose = new float[count * FLOATS_PER_BOX];
        this.active = new boolean[count];
        this.worldCache = new AzOrientedBox[count];
    }

    public AzHitboxSet set() {
        return set;
    }

    public int size() {
        return count;
    }

    public boolean isActive(int index) {
        return active[index];
    }

    public float referenceYaw() {
        return referenceYaw;
    }

    public long poseTime() {
        return poseTime;
    }

    public boolean isLive(long gameTime) {
        if (poseTime == Long.MIN_VALUE || gameTime - poseTime > set.poseTimeoutTicks()) {
            return false;
        }

        for (var a : active) {
            if (a) {
                return true;
            }
        }

        return false;
    }

    void setBox(
        int index,
        float cx,
        float cy,
        float cz,
        float hx,
        float hy,
        float hz,
        float qx,
        float qy,
        float qz,
        float qw
    ) {
        var o = index * FLOATS_PER_BOX;
        pose[o] = cx;
        pose[o + 1] = cy;
        pose[o + 2] = cz;
        pose[o + 3] = hx;
        pose[o + 4] = hy;
        pose[o + 5] = hz;
        pose[o + 6] = qx;
        pose[o + 7] = qy;
        pose[o + 8] = qz;
        pose[o + 9] = qw;
        active[index] = true;
    }

    void setInactive(int index) {
        active[index] = false;
    }

    void commit(Entity entity, long gameTime, float referenceYaw) {
        this.poseTime = gameTime;
        this.referenceYaw = referenceYaw;
        this.version++;

        worldBoxes(entity);
    }

    void invalidate() {
        Arrays.fill(active, false);
        this.poseTime = Long.MIN_VALUE;
        this.version++;
    }

    float localData(int index, int component) {
        return pose[index * FLOATS_PER_BOX + component];
    }

    public AzOrientedBox[] worldBoxes(Entity entity) {
        var x = entity.getX();
        var y = entity.getY();
        var z = entity.getZ();
        var yaw = AzHitboxes.bodyYaw(entity);
        long mask = enabledMask(entity);

        if (
            cacheVersion == version && cacheEnabledMask == mask
                && cacheX == x && cacheY == y && cacheZ == z && cacheYaw == yaw
        ) {
            return worldCache;
        }

        var theta = Math.toRadians(referenceYaw - yaw);
        var cos = Math.cos(theta);
        var sin = Math.sin(theta);
        var halfCos = Math.cos(theta * 0.5);
        var halfSin = Math.sin(theta * 0.5);
        AABB bounds = null;

        for (int i = 0; i < count; i++) {
            if (!active[i] || (mask & (1L << i)) == 0) {
                worldCache[i] = null;
                continue;
            }

            var o = i * FLOATS_PER_BOX;
            double lx = pose[o], ly = pose[o + 1], lz = pose[o + 2];
            double qx = pose[o + 6], qy = pose[o + 7], qz = pose[o + 8], qw = pose[o + 9];

            var wx = cos * lx + sin * lz;
            var wz = -sin * lx + cos * lz;

            var rx = halfCos * qx + halfSin * qz;
            var ry = halfCos * qy + halfSin * qw;
            var rz = halfCos * qz - halfSin * qx;
            var rw = halfCos * qw - halfSin * qy;

            var box = AzOrientedBox.of(x + wx, y + ly, z + wz, pose[o + 3], pose[o + 4], pose[o + 5], rx, ry, rz, rw);
            worldCache[i] = box;
            bounds = bounds == null ? box.bounds() : bounds.minmax(box.bounds());
        }

        cacheVersion = version;
        cacheEnabledMask = mask;
        cacheX = x;
        cacheY = y;
        cacheZ = z;
        cacheYaw = yaw;
        cacheBounds = bounds;

        if (bounds != null) {
            AzHitboxes.noteOverhang(entity.getBoundingBox(), bounds);
        }

        return worldCache;
    }

    public @Nullable AABB worldBounds(Entity entity) {
        worldBoxes(entity);
        return cacheBounds;
    }

    long enabledMask(Entity entity) {
        if (!(entity instanceof AzHitboxHolder holder)) {
            return -1L;
        }

        long mask = 0L;

        for (int i = 0; i < count; i++) {
            if (holder.isHitboxEnabled(set.get(i))) {
                mask |= 1L << i;
            }
        }

        return mask;
    }

    public boolean isEnabled(Entity entity, int index) {
        return !(entity instanceof AzHitboxHolder holder) || holder.isHitboxEnabled(set.get(index));
    }

    boolean hasEnabledActive(Entity entity) {
        long mask = enabledMask(entity);

        for (int i = 0; i < count; i++) {
            if (active[i] && (mask & (1L << i)) != 0) {
                return true;
            }
        }

        return false;
    }
}
