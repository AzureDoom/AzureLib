package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.HitResult;

import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

public final class AzHitboxCollision {

    private static final ThreadLocal<Context> CONTEXT = ThreadLocal.withInitial(Context::new);

    private AzHitboxCollision() {
        throw new UnsupportedOperationException();
    }

    public enum Query {

        PICK(AzHitbox::isPickable),
        PROJECTILE(AzHitbox::blocksProjectiles);

        private final Predicate<AzHitbox> filter;

        Query(Predicate<AzHitbox> filter) {
            this.filter = filter;
        }

        public Predicate<AzHitbox> filter() {
            return filter;
        }
    }

    private static final class Context {

        Query query = Query.PICK;

        @Nullable
        Entity projectile;

        @Nullable
        Entity candidate;
    }

    public static void begin(Query query, @Nullable Entity projectile) {
        Context ctx = CONTEXT.get();
        ctx.query = query;
        ctx.projectile = projectile;
        ctx.candidate = null;

        if (projectile != null && !Mc.isClientSide(Mc.world(projectile))) {
            AzHitboxDamageTracker.clearProjectileHits(projectile);
        }
    }

    /**
     * The projectile whose tick is running on this thread, or {@code null}. Beta projectiles deal damage from inside
     * their own tick and pass their shooter (not themselves) as the attacker, so this is how a hurt is matched to the
     * projectile that caused it.
     */
    @Nullable
    public static Entity activeProjectile() {
        Context ctx = CONTEXT.get();
        return ctx.query == Query.PROJECTILE ? ctx.projectile : null;
    }

    public static void endQuery() {
        Context ctx = CONTEXT.get();
        ctx.query = Query.PICK;
        ctx.projectile = null;
        ctx.candidate = null;
    }

    /**
     * Grows the area a projectile or the crosshair searches for entities by the largest hitbox overhang seen so far, so
     * an entity whose hitboxes reach into the area is a candidate even when its vanilla box doesn't. Extra candidates
     * are harmless: each one is still clipped exactly (by its hitboxes, or by vanilla for other entities).
     */
    public static Box widenSearch(Box area) {
        double overhang = AzHitboxes.maxOverhang();
        return overhang <= 0 ? area : AzHitboxMath.grow(area, overhang);
    }

    public static Box captureCandidate(Entity candidate) {
        CONTEXT.get().candidate = candidate;
        return Mc.boundingBox(candidate);
    }

    @Nullable
    private static AzHitboxState collidableState(Context ctx) {
        Entity candidate = ctx.candidate;

        if (candidate == null) {
            return null;
        }

        AzHitboxState state = AzHitboxes.getLiveState(candidate);

        if (state == null || !AzHitboxes.hasActive(state, ctx.query.filter())) {
            return null;
        }

        return state;
    }

    @Nullable
    public static HitResult clip(
        Box inflated,
        Vec3d from,
        Vec3d to,
        Supplier<HitResult> vanilla
    ) {
        Context ctx = CONTEXT.get();
        AzHitboxState state = collidableState(ctx);

        if (state == null) {
            return vanilla.get();
        }

        Entity candidate = ctx.candidate;
        double inflate = inflationOf(candidate, inflated);
        Optional<AzHitboxHit> hit = AzHitboxes.raycast(candidate, from, to, inflate, false, ctx.query.filter());
        boolean keepEntityBox = state.set().keepEntityBox();

        if (!hit.isPresent()) {
            return keepEntityBox ? vanilla.get() : null;
        }

        Vec3d location = hit.get().location();

        if (keepEntityBox) {
            // Which is nearer is worked out here so the vanilla hit result's fields never have to be read.
            double boxT = AzOrientedBox.of(inflated).clipFraction(from, to, false);

            if (
                boxT >= 0 && AzHitboxMath.distanceSqr(from, AzHitboxMath.lerp(from, to, boxT)) < AzHitboxMath
                    .distanceSqr(from, location)
            ) {
                return vanilla.get();
            }
        }

        Entity projectile = ctx.projectile;

        if (ctx.query == Query.PROJECTILE && projectile != null && !Mc.isClientSide(Mc.world(projectile))) {
            AzHitboxDamageTracker.recordProjectileHit(projectile, hit.get());
        }

        return Mc.hitResult(location);
    }

    public static boolean contains(Box inflated, Vec3d point, BooleanSupplier vanilla) {
        Context ctx = CONTEXT.get();
        AzHitboxState state = collidableState(ctx);

        if (state == null) {
            return vanilla.getAsBoolean();
        }

        Entity candidate = ctx.candidate;
        double inflate = inflationOf(candidate, inflated);
        AzOrientedBox[] boxes = state.worldBoxes(candidate);

        for (int i = 0; i < boxes.length; i++) {
            if (
                boxes[i] != null && ctx.query.filter().test(state.set().get(i)) && boxes[i].inflate(inflate)
                    .contains(point)
            ) {
                return true;
            }
        }

        return state.set().keepEntityBox() && vanilla.getAsBoolean();
    }

    private static double inflationOf(Entity candidate, Box inflated) {
        Box box = Mc.boundingBox(candidate);
        double inflate = ((inflated.maxX - inflated.minX) - (box.maxX - box.minX)) * 0.5;
        return Math.max(0, inflate);
    }
}
