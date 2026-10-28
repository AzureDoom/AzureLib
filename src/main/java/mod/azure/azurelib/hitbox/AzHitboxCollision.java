package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
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

        if (projectile != null && !Mc.isClientSide(projectile.world)) {
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List findCandidates(Box area, Function<Box, List> search) {
        double overhang = AzHitboxes.maxOverhang();

        if (overhang <= 0) {
            return search.apply(area);
        }

        List found = search.apply(AzHitboxMath.grow(area, overhang));
        List result = new ArrayList(found.size());

        for (Object object : found) {
            Entity entity = (Entity) object;

            if (entity.boundingBox.intersects(area)) {
                result.add(entity);
                continue;
            }

            AzHitboxState state = AzHitboxes.getLiveState(entity);

            if (state != null) {
                Box bounds = state.worldBounds(entity);

                if (bounds != null && bounds.intersects(area)) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    public static Box captureCandidate(Entity candidate) {
        CONTEXT.get().candidate = candidate;
        return candidate.boundingBox;
    }

    @Nullable
    private static AzHitboxState collidableState(Context ctx) {
        Entity candidate = ctx.candidate;

        if (candidate == null) {
            return null;
        }

        AzHitboxState state = AzHitboxes.getLiveState(candidate);

        if (state == null || !AzHitboxes.hasActive(candidate, state, ctx.query.filter())) {
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
            HitResult boxHit = vanilla.get();

            if (
                boxHit != null && boxHit.pos != null
                    && from.squaredDistanceTo(boxHit.pos) < from.squaredDistanceTo(location)
            ) {
                return boxHit;
            }
        }

        Entity projectile = ctx.projectile;

        if (ctx.query == Query.PROJECTILE && projectile != null && !Mc.isClientSide(projectile.world)) {
            AzHitboxDamageTracker.recordProjectileHit(projectile, hit.get());
        }

        return new HitResult(0, 0, 0, 1, location);
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
        Box box = candidate.boundingBox;
        double inflate = ((inflated.maxX - inflated.minX) - (box.maxX - box.minX)) * 0.5;
        return Math.max(0, inflate);
    }
}
