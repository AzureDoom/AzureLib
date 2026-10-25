package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;

import javax.annotation.Nullable;

import mod.azure.azurelib.network.packet.AzHitboxPosePacket;
import mod.azure.azurelib.platform.mc.Mc;

public final class AzHitboxSync {

    private static final double TAKEOVER_RATIO = 0.75;

    private static final double MAX_REPORT_DISTANCE = 128;

    private AzHitboxSync() {
        throw new UnsupportedOperationException();
    }

    public static boolean isReportDue(AzHitboxState state, long gameTime) {
        if (!state.set().serverSync()) {
            return false;
        }

        return state.lastReportTime == Long.MIN_VALUE
            || gameTime - state.lastReportTime >= state.set().reportIntervalTicks();
    }

    public static boolean shouldReport(AzHitboxState state, long gameTime) {
        if (!isReportDue(state, gameTime)) {
            return false;
        }

        state.lastReportTime = gameTime;
        return true;
    }

    @Nullable
    public static AzHitboxPosePacket buildReport(Entity entity, AzHitboxState state) {
        int count = state.size();
        boolean[] active = new boolean[count];
        int activeCount = 0;

        for (int i = 0; i < count; i++) {
            active[i] = state.isActive(i);

            if (active[i]) {
                activeCount++;
            }
        }

        if (activeCount == 0) {
            return null;
        }

        float[] data = new float[activeCount * AzHitboxState.FLOATS_PER_BOX];
        int o = 0;

        for (int i = 0; i < count; i++) {
            if (!active[i]) {
                continue;
            }

            for (int c = 0; c < AzHitboxState.FLOATS_PER_BOX; c++) {
                data[o++] = state.localData(i, c);
            }
        }

        return new AzHitboxPosePacket(Mc.entityId(entity), state.referenceYaw(), active, data);
    }

    public static void acceptReport(
        PlayerEntity sender,
        Entity entity,
        float referenceYaw,
        boolean[] active,
        float[] data
    ) {
        AzHitboxState state = AzHitboxes.getOrCreateState(entity);

        if (state == null || !state.set().serverSync() || active.length != state.size()) {
            return;
        }

        double distanceSqr = Mc.distanceSq(sender, entity);

        if (distanceSqr > MAX_REPORT_DISTANCE * MAX_REPORT_DISTANCE || !isFinite(referenceYaw)) {
            return;
        }

        long now = Mc.time(Mc.world(entity));

        if (!isReporter(state, sender, distanceSqr, now)) {
            return;
        }

        AzHitboxSet set = state.set();
        double maxReach = set.resolveMaxReach(entity);
        double maxReachSqr = maxReach * maxReach;
        float maxHalf = set.maxHalfExtent();
        int o = 0;

        for (int i = 0; i < active.length; i++) {
            if (!active[i]) {
                state.setInactive(i);
                continue;
            }

            float cx = data[o];
            float cy = data[o + 1];
            float cz = data[o + 2];
            float hx = data[o + 3];
            float hy = data[o + 4];
            float hz = data[o + 5];
            float qx = data[o + 6];
            float qy = data[o + 7];
            float qz = data[o + 8];
            float qw = data[o + 9];
            o += AzHitboxState.FLOATS_PER_BOX;

            if (!allFinite(cx, cy, cz, hx, hy, hz, qx, qy, qz, qw)) {
                state.setInactive(i);
                continue;
            }

            if ((double) cx * cx + (double) cy * cy + (double) cz * cz > maxReachSqr) {
                state.setInactive(i);
                continue;
            }

            state.setBox(
                i,
                cx,
                cy,
                cz,
                clamp(hx, maxHalf),
                clamp(hy, maxHalf),
                clamp(hz, maxHalf),
                qx,
                qy,
                qz,
                qw
            );
        }

        state.commit(entity, now, referenceYaw);
    }

    private static boolean isReporter(AzHitboxState state, PlayerEntity sender, double distanceSqr, long now) {
        boolean stale = state.reporterTime == Long.MIN_VALUE || now - state.reporterTime > state.set()
            .poseTimeoutTicks();

        int senderId = Mc.entityId(sender);

        if (
            state.reporterId == -1 || stale || senderId == state.reporterId
                || distanceSqr < state.reporterDistanceSqr * TAKEOVER_RATIO
        ) {
            state.reporterId = senderId;
            state.reporterDistanceSqr = distanceSqr;
            state.reporterTime = now;
            return true;
        }

        return false;
    }

    private static boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    private static boolean allFinite(float... values) {
        for (float v : values) {
            if (!isFinite(v)) {
                return false;
            }
        }

        return true;
    }

    private static float clamp(float half, float max) {
        return Math.max(0, Math.min(Math.abs(half), max));
    }
}
