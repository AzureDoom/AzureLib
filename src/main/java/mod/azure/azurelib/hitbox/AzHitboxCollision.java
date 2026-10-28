package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

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

        if (projectile != null && !projectile.worldObj.isRemote) {
            AzHitboxDamageTracker.clearProjectileHits(projectile);
        }
    }

    public static void endQuery() {
        Context ctx = CONTEXT.get();
        ctx.projectile = null;
        ctx.candidate = null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List findCandidates(AxisAlignedBB area, Function<AxisAlignedBB, List> search) {
        double overhang = AzHitboxes.maxOverhang();

        if (overhang <= 0) {
            return search.apply(area);
        }

        List found = search.apply(AzHitboxMath.grow(area, overhang));
        List result = new ArrayList(found.size());

        for (Object object : found) {
            Entity entity = (Entity) object;

            if (entity.boundingBox.intersectsWith(area)) {
                result.add(entity);
                continue;
            }

            AzHitboxState state = AzHitboxes.getLiveState(entity);

            if (state != null) {
                AxisAlignedBB bounds = state.worldBounds(entity);

                if (bounds != null && bounds.intersectsWith(area)) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    public static AxisAlignedBB captureCandidate(Entity candidate) {
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
    public static MovingObjectPosition clip(
        AxisAlignedBB inflated,
        Vec3 from,
        Vec3 to,
        Supplier<MovingObjectPosition> vanilla
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

        Vec3 location = hit.get().location();

        if (keepEntityBox) {
            MovingObjectPosition boxHit = vanilla.get();

            if (
                boxHit != null && boxHit.hitVec != null
                    && from.squareDistanceTo(boxHit.hitVec) < from.squareDistanceTo(location)
            ) {
                return boxHit;
            }
        }

        Entity projectile = ctx.projectile;

        if (ctx.query == Query.PROJECTILE && projectile != null && !projectile.worldObj.isRemote) {
            AzHitboxDamageTracker.recordProjectileHit(projectile, hit.get());
        }

        return new MovingObjectPosition(0, 0, 0, 1, location);
    }

    public static boolean contains(AxisAlignedBB inflated, Vec3 point, BooleanSupplier vanilla) {
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

    private static double inflationOf(Entity candidate, AxisAlignedBB inflated) {
        AxisAlignedBB box = candidate.boundingBox;
        double inflate = ((inflated.maxX - inflated.minX) - (box.maxX - box.minX)) * 0.5;
        return Math.max(0, inflate);
    }
}
