package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

public final class AzHitboxDamageTracker {

    private static final double SERVER_REACH = 6.0;

    private static final double CLAIM_TOLERANCE = 1.5;

    private static final Map<EntityPlayerMP, Claim> MELEE_CLAIMS = new WeakHashMap<>();

    private static final Map<EntityPlayerMP, Claim> INTERACT_CLAIMS = new WeakHashMap<>();

    private static final Map<Entity, Map<Integer, Claim>> PROJECTILE_CLAIMS = new WeakHashMap<>();

    private AzHitboxDamageTracker() {
        throw new UnsupportedOperationException();
    }

    private static final class Claim {

        final int targetId;

        final int index;

        final Vec3 location;

        final long time;

        Claim(int targetId, int index, Vec3 location, long time) {
            this.targetId = targetId;
            this.index = index;
            this.location = location;
            this.time = time;
        }
    }

    public static void claimMelee(EntityPlayerMP player, Entity target, int index) {
        Claim claim = validateClaim(player, target, index, AzHitboxRole.COMBAT);

        if (claim != null) {
            MELEE_CLAIMS.put(player, claim);
        }
    }

    public static void claimInteract(EntityPlayerMP player, Entity target, int index) {
        Claim claim = validateClaim(player, target, index, AzHitboxRole.INTERACT);

        if (claim != null) {
            INTERACT_CLAIMS.put(player, claim);
        }
    }

    @Nullable
    private static Claim validateClaim(EntityPlayerMP player, Entity target, int index, AzHitboxRole role) {
        AzHitboxState state = AzHitboxes.getOrCreateState(target);

        if (state == null || index < 0 || index >= state.size() || state.set().get(index).role() != role) {
            return null;
        }

        Vec3 eye = AzHitboxMath.eyePosition(player);
        double reach = SERVER_REACH + CLAIM_TOLERANCE;
        long now = target.worldObj.getTotalWorldTime();
        Vec3 location;

        if (state.isLive(now)) {
            AzOrientedBox box = state.worldBoxes(target)[index];

            if (box == null || box.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            Vec3 end = AzHitboxMath.along(eye, AzHitboxMath.look(player), reach + 1);
            double t = box.clipFraction(eye, end, true);
            location = t >= 0 ? AzHitboxMath.lerp(eye, end, t) : box.closestPoint(eye);
        } else {
            double maxReach = state.set().resolveMaxReach(target);
            AxisAlignedBB bounds = target.boundingBox;
            AzOrientedBox area = AzOrientedBox.of(AzHitboxMath.grow(bounds, maxReach));

            if (area.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            location = Vec3.createVectorHelper(
                (bounds.minX + bounds.maxX) * 0.5,
                (bounds.minY + bounds.maxY) * 0.5,
                (bounds.minZ + bounds.maxZ) * 0.5
            );
        }

        return new Claim(target.getEntityId(), index, location, now);
    }

    public static void recordProjectileHit(Entity projectile, AzHitboxHit hit) {
        Map<Integer, Claim> claims = PROJECTILE_CLAIMS.get(projectile);

        if (claims == null) {
            claims = new HashMap<>();
            PROJECTILE_CLAIMS.put(projectile, claims);
        }

        claims.put(
            hit.entity().getEntityId(),
            new Claim(hit.entity().getEntityId(), hit.index(), hit.location(), projectile.worldObj.getTotalWorldTime())
        );
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

        Claim claim = claims.remove(target.getEntityId());

        if (claims.isEmpty()) {
            PROJECTILE_CLAIMS.remove(projectile);
        }

        return claim != null && now - claim.time <= 1 ? claim : null;
    }

    public static float applyHurt(EntityLivingBase target, DamageSource source, float amount) {
        if (!(target instanceof AzHitboxHolder)) {
            return amount;
        }

        AzHitboxHit hit = resolveDamage(target, source);

        if (hit == null) {
            return amount;
        }

        AzHitboxState state = AzHitboxes.getState(target);

        if (state != null) {
            state.lastHit = hit;
            state.lastHitTime = target.worldObj.getTotalWorldTime();
        }

        return ((AzHitboxHolder) target).onHitboxHit(hit, source, amount);
    }

    @Nullable
    public static AzHitboxHit takeInteract(EntityPlayerMP player, Entity target) {
        Claim claim = take(INTERACT_CLAIMS, player, target, target.worldObj.getTotalWorldTime());
        return claim == null ? null : toHit(target, claim);
    }

    @Nullable
    private static AzHitboxHit resolveDamage(Entity target, DamageSource source) {
        Entity direct = source.getSourceOfDamage();

        if (direct == null) {
            return null;
        }

        long now = target.worldObj.getTotalWorldTime();
        Claim claim = null;

        if (direct instanceof EntityPlayerMP && source.getEntity() == direct) {
            claim = take(MELEE_CLAIMS, (EntityPlayerMP) direct, target, now);
        }

        if (claim == null) {
            claim = takeProjectile(direct, target, now);
        }

        return claim == null ? null : toHit(target, claim);
    }

    @Nullable
    private static AzHitboxHit toHit(Entity target, Claim claim) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || claim.index >= state.size()) {
            return null;
        }

        long now = target.worldObj.getTotalWorldTime();
        AzOrientedBox box = state.isLive(now) ? state.worldBoxes(target)[claim.index] : null;

        if (box == null) {
            box = AzOrientedBox.of(
                claim.location.xCoord,
                claim.location.yCoord,
                claim.location.zCoord,
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

        if (claim == null || claim.targetId != target.getEntityId()) {
            return null;
        }

        claims.remove(key);
        return now - claim.time <= 1 ? claim : null;
    }
}
