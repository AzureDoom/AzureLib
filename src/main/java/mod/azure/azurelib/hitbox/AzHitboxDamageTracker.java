package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;

public final class AzHitboxDamageTracker {

    private static final double SERVER_REACH = 6.0;

    private static final double CLAIM_TOLERANCE = 1.5;

    private static final Map<EntityPlayerMP, Claim> MELEE_CLAIMS = new WeakHashMap<>();

    private static final Map<EntityPlayerMP, Claim> INTERACT_CLAIMS = new WeakHashMap<>();

    private static final Map<Entity, Claim> PROJECTILE_CLAIMS = new WeakHashMap<>();

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

        if (
                state == null || index < 0 || index >= state.size()
                        || state.set().get(index).role() != role
                        || !state.isEnabled(target, index)
        ) {
            return null;
        }

        Vec3d eye = player.getPositionEyes(1f);
        double reach = SERVER_REACH + CLAIM_TOLERANCE;
        long now = target.world.getTotalWorldTime();
        Vec3d location;

        if (state.isLive(now)) {
            AzOrientedBox box = state.worldBoxes(target)[index];

            if (box == null || box.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            Vec3d end = eye.add(player.getLook(1f).scale(reach + 1));
            double t = box.clipFraction(eye, end, true);
            location = t >= 0 ? eye.add(end.subtract(eye).scale(t)) : box.closestPoint(eye);
        } else {
            double maxReach = state.set().resolveMaxReach(target);
            AxisAlignedBB bounds = target.getEntityBoundingBox();
            AzOrientedBox area = AzOrientedBox.of(bounds.grow(maxReach));

            if (area.distanceToSqr(eye) > reach * reach) {
                return null;
            }

            location = new Vec3d(
                (bounds.minX + bounds.maxX) * 0.5,
                (bounds.minY + bounds.maxY) * 0.5,
                (bounds.minZ + bounds.maxZ) * 0.5
            );
        }

        return new Claim(target.getEntityId(), index, location, now);
    }

    public static void recordProjectileHit(Entity projectile, AzHitboxHit hit) {
        PROJECTILE_CLAIMS.put(
            projectile,
            new Claim(hit.entity().getEntityId(), hit.index(), hit.location(), projectile.world.getTotalWorldTime())
        );
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
            state.lastHitTime = target.world.getTotalWorldTime();
            state.pendingHit = hit;
            state.pendingSource = source;
            state.pendingAccepted = false;
        }

        return ((AzHitboxHolder) target).onHitboxHit(hit, source, amount);
    }

    public static void confirmHurt(EntityLivingBase target, DamageSource source, float amount) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || state.pendingHit == null || state.pendingSource != source) {
            return;
        }

        if (amount <= 0) {
            clearPending(state);
            return;
        }

        state.pendingAmount = amount;
        state.pendingAccepted = true;
    }

    public static void fireDamaged(EntityLivingBase target, DamageSource source) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || !state.pendingAccepted || state.pendingHit == null || state.pendingSource != source) {
            return;
        }

        AzHitboxHit hit = state.pendingHit;
        float amount = state.pendingAmount;
        clearPending(state);

        ((AzHitboxHolder) target).onHitboxDamaged(hit, source, amount);
    }

    private static void clearPending(AzHitboxState state) {
        state.pendingHit = null;
        state.pendingSource = null;
        state.pendingAmount = 0;
        state.pendingAccepted = false;
    }

    @Nullable
    public static AzHitboxHit takeInteract(EntityPlayerMP player, Entity target) {
        Claim claim = take(INTERACT_CLAIMS, player, target, target.world.getTotalWorldTime());
        return claim == null ? null : toHit(target, claim);
    }

    @Nullable
    private static AzHitboxHit resolveDamage(Entity target, DamageSource source) {
        Entity direct = source.getImmediateSource();

        if (direct == null) {
            return null;
        }

        long now = target.world.getTotalWorldTime();
        Claim claim = null;

        if (direct instanceof EntityPlayerMP && source.getTrueSource() == direct) {
            claim = take(MELEE_CLAIMS, (EntityPlayerMP) direct, target, now);
        }

        if (claim == null) {
            claim = take(PROJECTILE_CLAIMS, direct, target, now);
        }

        return claim == null ? null : toHit(target, claim);
    }

    @Nullable
    private static AzHitboxHit toHit(Entity target, Claim claim) {
        AzHitboxState state = AzHitboxes.getState(target);

        if (state == null || claim.index >= state.size() || !state.isEnabled(target, claim.index)) {
            return null;
        }

        long now = target.world.getTotalWorldTime();
        AzOrientedBox box = state.isLive(now) ? state.worldBoxes(target)[claim.index] : null;

        if (box == null) {
            box = AzOrientedBox.of(claim.location.x, claim.location.y, claim.location.z, 0, 0, 0, 0, 0, 0, 1);
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
