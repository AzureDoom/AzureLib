package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;

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

    private AxisAlignedBB cacheBounds;

    UUID fitModel;

    float[][] fitCache;

    long lastReportTime = Long.MIN_VALUE;

    long lastRenderCaptureTime = Long.MIN_VALUE;

    UUID reporter;

    double reporterDistanceSqr;

    long reporterTime = Long.MIN_VALUE;

    AzHitboxHit lastHit;

    long lastHitTime = Long.MIN_VALUE;

    AzHitboxHit pendingHit;

    DamageSource pendingSource;

    float pendingAmount;

    boolean pendingAccepted;

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

        for (boolean a : active) {
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
        int o = index * FLOATS_PER_BOX;
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
        double x = entity.posX;
        double y = entity.posY;
        double z = entity.posZ;
        float yaw = AzHitboxes.bodyYaw(entity);
        long mask = enabledMask(entity);

        if (
            cacheVersion == version && cacheEnabledMask == mask
                && cacheX == x && cacheY == y && cacheZ == z && cacheYaw == yaw
        ) {
            return worldCache;
        }

        double theta = Math.toRadians(referenceYaw - yaw);
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double halfCos = Math.cos(theta * 0.5);
        double halfSin = Math.sin(theta * 0.5);
        AxisAlignedBB bounds = null;

        for (int i = 0; i < count; i++) {
            if (!active[i] || (mask & (1L << i)) == 0) {
                worldCache[i] = null;
                continue;
            }

            int o = i * FLOATS_PER_BOX;
            double lx = pose[o], ly = pose[o + 1], lz = pose[o + 2];
            double qx = pose[o + 6], qy = pose[o + 7], qz = pose[o + 8], qw = pose[o + 9];

            double wx = cos * lx + sin * lz;
            double wz = -sin * lx + cos * lz;

            double rx = halfCos * qx + halfSin * qz;
            double ry = halfCos * qy + halfSin * qw;
            double rz = halfCos * qz - halfSin * qx;
            double rw = halfCos * qw - halfSin * qy;

            AzOrientedBox box = AzOrientedBox.of(
                x + wx,
                y + ly,
                z + wz,
                pose[o + 3],
                pose[o + 4],
                pose[o + 5],
                rx,
                ry,
                rz,
                rw
            );
            worldCache[i] = box;
            bounds = bounds == null ? box.bounds() : AzHitboxMath.union(bounds, box.bounds());
        }

        cacheVersion = version;
        cacheEnabledMask = mask;
        cacheX = x;
        cacheY = y;
        cacheZ = z;
        cacheYaw = yaw;
        cacheBounds = bounds;

        if (bounds != null) {
            AzHitboxes.noteOverhang(entity.boundingBox, bounds);
        }

        return worldCache;
    }

    public AxisAlignedBB worldBounds(Entity entity) {
        worldBoxes(entity);
        return cacheBounds;
    }

    long enabledMask(Entity entity) {
        if (!(entity instanceof AzHitboxHolder)) {
            return -1L;
        }

        long mask = 0L;

        for (int i = 0; i < count; i++) {
            if (((AzHitboxHolder) entity).isHitboxEnabled(set.get(i))) {
                mask |= 1L << i;
            }
        }

        return mask;
    }

    public boolean isEnabled(Entity entity, int index) {
        return !(entity instanceof AzHitboxHolder) || ((AzHitboxHolder) entity).isHitboxEnabled(set.get(index));
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
