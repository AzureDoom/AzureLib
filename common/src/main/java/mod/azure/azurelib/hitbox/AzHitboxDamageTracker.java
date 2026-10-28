package mod.azure.azurelib.hitbox;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public final class AzHitboxDamageTracker {

    private static final double CLAIM_TOLERANCE = 1.5;

    private static final Map<ServerPlayer, Claim> MELEE_CLAIMS = new WeakHashMap<>();

    private static final Map<ServerPlayer, Claim> INTERACT_CLAIMS = new WeakHashMap<>();

    private static final Map<Entity, Claim> PROJECTILE_CLAIMS = new WeakHashMap<>();

    private AzHitboxDamageTracker() {
        throw new UnsupportedOperationException();
    }

    private record Claim(
        int targetId,
        int index,
        Vec3 location,
        long time
    ) {}

    public static void claimMelee(ServerPlayer player, Entity target, int index) {
        var claim = validateClaim(player, target, index, AzHitboxRole.COMBAT);

        if (claim != null) {
            MELEE_CLAIMS.put(player, claim);
        }
    }

    public static void claimInteract(ServerPlayer player, Entity target, int index) {
        var claim = validateClaim(player, target, index, AzHitboxRole.INTERACT);

        if (claim != null) {
            INTERACT_CLAIMS.put(player, claim);
        }
    }

    private static @Nullable Claim validateClaim(ServerPlayer player, Entity target, int index, AzHitboxRole role) {
        var state = AzHitboxes.getOrCreateState(target);

        if (
            state == null || index < 0 || index >= state.size()
                || state.set().get(index).role() != role
                || !state.isEnabled(target, index)
        ) {
            return null;
        }

        var eye = player.getEyePosition();
        var reach = player.entityInteractionRange() + CLAIM_TOLERANCE;
        var now = target.level().getGameTime();
        Vec3 location;

        if (state.isLive(now)) {
            var box = state.worldBoxes(target)[index];

            if (box == null || box.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            var end = eye.add(player.getViewVector(1f).scale(reach + 1));
            var t = box.clipFraction(eye, end, true);
            location = t >= 0 ? eye.add(end.subtract(eye).scale(t)) : box.closestPoint(eye);
        } else {
            var maxReach = state.set().resolveMaxReach(target);
            var area = AzOrientedBox.of(target.getBoundingBox().inflate(maxReach));

            if (area.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            location = target.getBoundingBox().getCenter();
        }

        return new Claim(target.getId(), index, location, now);
    }

    public static void recordProjectileHit(Entity projectile, AzHitboxHit hit) {
        PROJECTILE_CLAIMS.put(
            projectile,
            new Claim(hit.entity().getId(), hit.index(), hit.location(), projectile.level().getGameTime())
        );
    }

    public static float applyHurt(LivingEntity target, DamageSource source, float amount) {
        if (!(target instanceof AzHitboxHolder holder)) {
            return amount;
        }

        var hit = resolveDamage(target, source);

        if (hit == null) {
            return amount;
        }

        var state = AzHitboxes.getState(target);

        if (state != null) {
            state.lastHit = hit;
            state.lastHitTime = target.level().getGameTime();
            state.pendingHit = hit;
            state.pendingSource = source;
        }

        return holder.onHitboxHit(hit, source, amount);
    }

    public static @Nullable AzHitboxHit takeAccepted(LivingEntity target, DamageSource source) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || state.pendingHit == null || state.pendingSource != source) {
            return null;
        }

        AzHitboxHit hit = state.pendingHit;
        state.pendingHit = null;
        state.pendingSource = null;

        return target.isInvulnerableTo((ServerLevel) target.level(), source) ? null : hit;
    }

    public static @Nullable AzHitboxHit takeInteract(ServerPlayer player, Entity target) {
        var claim = take(INTERACT_CLAIMS, player, target, target.level().getGameTime());
        return claim == null ? null : toHit(target, claim);
    }

    private static @Nullable AzHitboxHit resolveDamage(Entity target, DamageSource source) {
        var direct = source.getDirectEntity();

        if (direct == null) {
            return null;
        }

        var now = target.level().getGameTime();
        Claim claim = null;

        if (direct instanceof ServerPlayer player && source.getEntity() == player) {
            claim = take(MELEE_CLAIMS, player, target, now);
        }

        if (claim == null) {
            claim = take(PROJECTILE_CLAIMS, direct, target, now);
        }

        return claim == null ? null : toHit(target, claim);
    }

    private static @Nullable AzHitboxHit toHit(Entity target, Claim claim) {
        var state = AzHitboxes.getState(target);

        if (state == null || claim.index >= state.size() || !state.isEnabled(target, claim.index)) {
            return null;
        }

        var now = target.level().getGameTime();
        var box = state.isLive(now) ? state.worldBoxes(target)[claim.index] : null;

        if (box == null) {
            box = AzOrientedBox.of(claim.location.x, claim.location.y, claim.location.z, 0, 0, 0, 0, 0, 0, 1);
        }

        return new AzHitboxHit(target, state.set().get(claim.index), claim.index, box, claim.location);
    }

    private static <K> @Nullable Claim take(Map<K, Claim> claims, K key, Entity target, long now) {
        var claim = claims.get(key);

        if (claim == null || claim.targetId != target.getId()) {
            return null;
        }

        claims.remove(key);
        return now - claim.time <= 1 ? claim : null;
    }
}
