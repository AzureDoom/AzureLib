package mod.azure.azurelib.hitbox;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.network.packet.AzHitboxPosePacket;

public final class AzHitboxSync {

    private static final double TAKEOVER_RATIO = 0.75;

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
        if (!state.set().serverSync()) {
            return false;
        }

        if (
            state.lastReportTime != Long.MIN_VALUE && gameTime - state.lastReportTime < state.set()
                .reportIntervalTicks()
        ) {
            return false;
        }

        state.lastReportTime = gameTime;
        return true;
    }

    public static @Nullable AzHitboxPosePacket buildReport(Entity entity, AzHitboxState state) {
        var count = state.size();
        var active = new boolean[count];
        var activeCount = 0;

        for (int i = 0; i < count; i++) {
            active[i] = state.isActive(i);

            if (active[i]) {
                activeCount++;
            }
        }

        if (activeCount == 0) {
            return null;
        }

        var data = new float[activeCount * AzHitboxState.FLOATS_PER_BOX];
        var o = 0;

        for (int i = 0; i < count; i++) {
            if (!active[i]) {
                continue;
            }

            for (int c = 0; c < AzHitboxState.FLOATS_PER_BOX; c++) {
                data[o++] = state.localData(i, c);
            }
        }

        return new AzHitboxPosePacket(entity.getId(), state.referenceYaw(), active, data);
    }

    public static void acceptReport(
        ServerPlayer sender,
        Entity entity,
        float referenceYaw,
        boolean[] active,
        float[] data
    ) {
        var state = AzHitboxes.getOrCreateState(entity);

        if (state == null || !state.set().serverSync() || active.length != state.size()) {
            return;
        }

        var distanceSqr = sender.distanceToSqr(entity);
        var trackingRange = Math.max(1, entity.getType().clientTrackingRange()) * 16.0;

        if (distanceSqr > trackingRange * trackingRange || !Float.isFinite(referenceYaw)) {
            return;
        }

        var now = entity.level.getGameTime();

        if (!isReporter(state, sender, distanceSqr, now)) {
            return;
        }

        var set = state.set();
        var maxReach = set.resolveMaxReach(entity);
        var maxReachSqr = maxReach * maxReach;
        var maxHalf = set.maxHalfExtent();
        var o = 0;

        for (int i = 0; i < active.length; i++) {
            if (!active[i]) {
                state.setInactive(i);
                continue;
            }

            var cx = data[o];
            var cy = data[o + 1];
            var cz = data[o + 2];
            var hx = data[o + 3];
            var hy = data[o + 4];
            var hz = data[o + 5];
            var qx = data[o + 6];
            var qy = data[o + 7];
            var qz = data[o + 8];
            var qw = data[o + 9];
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

    private static boolean isReporter(AzHitboxState state, ServerPlayer sender, double distanceSqr, long now) {
        var uuid = sender.getUUID();
        var stale = state.reporterTime == Long.MIN_VALUE || now - state.reporterTime > state.set().poseTimeoutTicks();

        if (
            state.reporter == null || stale || uuid.equals(state.reporter)
                || distanceSqr < state.reporterDistanceSqr * TAKEOVER_RATIO
        ) {
            state.reporter = uuid;
            state.reporterDistanceSqr = distanceSqr;
            state.reporterTime = now;
            return true;
        }

        return false;
    }

    private static boolean allFinite(float... values) {
        for (var v : values) {
            if (!Float.isFinite(v)) {
                return false;
            }
        }

        return true;
    }

    private static float clamp(float half, float max) {
        return Math.max(0, Math.min(Math.abs(half), max));
    }
}
