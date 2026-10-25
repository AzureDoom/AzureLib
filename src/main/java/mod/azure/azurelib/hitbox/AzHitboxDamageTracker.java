package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

/**
 * Matches incoming damage to the hitbox that caused it.
 * <p>
 * Beta has no {@code DamageSource}: {@code LivingEntity#damage(Entity, int)} only receives an attacker, and projectiles
 * pass their shooter rather than themselves. So instead of reading the source, AzureLib tracks what is happening on
 * this thread when the damage lands:
 * <ul>
 * <li>a melee hit runs inside {@code PlayerEntity#attack(Entity)}, which {@link #beginMelee}/{@link #endMelee}
 * bracket;</li>
 * <li>a projectile hit runs inside the projectile's tick, which {@link AzHitboxCollision} brackets.</li>
 * </ul>
 * Claims are keyed by {@link PlayerEntity} rather than the server player class, which does not exist on the client,
 * because Beta single player runs the "server" logic on the client.
 */
public final class AzHitboxDamageTracker {

    private static final double SERVER_REACH = 6.0;

    private static final double CLAIM_TOLERANCE = 1.5;

    private static final Map<PlayerEntity, Claim> MELEE_CLAIMS = new WeakHashMap<>();

    private static final Map<PlayerEntity, Claim> INTERACT_CLAIMS = new WeakHashMap<>();

    private static final Map<Entity, Map<Integer, Claim>> PROJECTILE_CLAIMS = new WeakHashMap<>();

    @Nullable
    private static PlayerEntity meleeAttacker;

    @Nullable
    private static Entity meleeTarget;

    private AzHitboxDamageTracker() {
        throw new UnsupportedOperationException();
    }

    private static final class Claim {

        final int targetId;

        final int index;

        final Vec3d location;

        final long time;

        Claim(int targetId, int index, Vec3d location, long time) {
            this.targetId = targetId;
            this.index = index;
            this.location = location;
            this.time = time;
        }
    }

    public static void claimMelee(PlayerEntity player, Entity target, int index) {
        Claim claim = validateClaim(player, target, index, AzHitboxRole.COMBAT);

        if (claim != null) {
            MELEE_CLAIMS.put(player, claim);
        }
    }

    public static void claimInteract(PlayerEntity player, Entity target, int index) {
        Claim claim = validateClaim(player, target, index, AzHitboxRole.INTERACT);

        if (claim != null) {
            INTERACT_CLAIMS.put(player, claim);
        }
    }

    /** Called at the start of {@code PlayerEntity#attack(Entity)}. */
    public static void beginMelee(PlayerEntity player, Entity target) {
        meleeAttacker = player;
        meleeTarget = target;
    }

    /** Called when {@code PlayerEntity#attack(Entity)} returns. */
    public static void endMelee() {
        meleeAttacker = null;
        meleeTarget = null;
    }

    @Nullable
    private static Claim validateClaim(PlayerEntity player, Entity target, int index, AzHitboxRole role) {
        AzHitboxState state = AzHitboxes.getOrCreateState(target);

        if (state == null || index < 0 || index >= state.size() || state.set().get(index).role() != role) {
            return null;
        }

        Vec3d eye = AzHitboxMath.eyePosition(player);
        double reach = SERVER_REACH + CLAIM_TOLERANCE;
        long now = Mc.time(target.world);
        Vec3d location;

        if (state.isLive(now)) {
            AzOrientedBox box = state.worldBoxes(target)[index];

            if (box == null || box.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            Vec3d end = AzHitboxMath.along(eye, AzHitboxMath.look(player), reach + 1);
            double t = box.clipFraction(eye, end, true);
            location = t >= 0 ? AzHitboxMath.lerp(eye, end, t) : box.closestPoint(eye);
        } else {
            double maxReach = state.set().resolveMaxReach(target);
            Box bounds = target.boundingBox;
            AzOrientedBox area = AzOrientedBox.of(AzHitboxMath.grow(bounds, maxReach));

            if (area.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            location = AzHitboxMath.center(bounds);
        }

        return new Claim(Mc.entityId(target), index, location, now);
    }

    public static void recordProjectileHit(Entity projectile, AzHitboxHit hit) {
        Map<Integer, Claim> claims = PROJECTILE_CLAIMS.get(projectile);

        if (claims == null) {
            claims = new HashMap<>();
            PROJECTILE_CLAIMS.put(projectile, claims);
        }

        int targetId = Mc.entityId(hit.entity());
        claims.put(targetId, new Claim(targetId, hit.index(), hit.location(), Mc.time(projectile.world)));
    }

    public static void clearProjectileHits(Entity projectile) {
        PROJECTILE_CLAIMS.remove(projectile);
    }

    @Nullable
    private static Claim takeProjectile(Entity projectile, Entity target, long now) {
        Map<Integer, Claim> claims = PROJECTILE_CLAIMS.get(projectile);

        if (claims == null) {
            return null;
        }

        Claim claim = claims.remove(Mc.entityId(target));

        if (claims.isEmpty()) {
            PROJECTILE_CLAIMS.remove(projectile);
        }

        return claim != null && now - claim.time <= 1 ? claim : null;
    }

    /** Runs at the start of {@code LivingEntity#damage(Entity, int)} and returns the damage to apply. */
    public static int applyHurt(LivingEntity target, int amount) {
        if (!(target instanceof AzHitboxHolder) || Mc.isClientSide(target.world)) {
            return amount;
        }

        Entity source = null;
        Claim claim = null;
        long now = Mc.time(target.world);
        Entity projectile = AzHitboxCollision.activeProjectile();

        if (projectile != null) {
            source = projectile;
            claim = takeProjectile(projectile, target, now);
        } else if (meleeAttacker != null && meleeTarget == target) {
            source = meleeAttacker;
            claim = take(MELEE_CLAIMS, meleeAttacker, target, now);
        }

        AzHitboxHit hit = claim == null ? null : toHit(target, claim);

        if (hit == null) {
            return amount;
        }

        AzHitboxState state = AzHitboxes.getState(target);

        if (state != null) {
            state.lastHit = hit;
            state.lastHitTime = now;
        }

        return ((AzHitboxHolder) target).onHitboxHit(hit, source, amount);
    }

    @Nullable
    public static AzHitboxHit takeInteract(PlayerEntity player, Entity target) {
        Claim claim = take(INTERACT_CLAIMS, player, target, Mc.time(target.world));
        return claim == null ? null : toHit(target, claim);
    }

    @Nullable
    private static AzHitboxHit toHit(Entity target, Claim claim) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || claim.index >= state.size()) {
            return null;
        }

        long now = Mc.time(target.world);
        AzOrientedBox box = state.isLive(now) ? state.worldBoxes(target)[claim.index] : null;

        if (box == null) {
            box = AzOrientedBox.of(
                claim.location.x,
                claim.location.y,
                claim.location.z,
                0,
                0,
                0,
                0,
                0,
                0,
                1
            );
        }

        return new AzHitboxHit(target, state.set().get(claim.index), claim.index, box, claim.location);
    }

    @Nullable
    private static <K> Claim take(Map<K, Claim> claims, K key, Entity target, long now) {
        Claim claim = claims.get(key);

        if (claim == null || claim.targetId != Mc.entityId(target)) {
            return null;
        }

        claims.remove(key);
        return now - claim.time <= 1 ? claim : null;
    }
}
