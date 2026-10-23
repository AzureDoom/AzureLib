package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
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
        Entity candidate;

        final Map<Entity, AzHitboxHit> hits = new IdentityHashMap<>();
    }

    public static void begin(Query query) {
        Context ctx = CONTEXT.get();
        ctx.query = query;
        ctx.candidate = null;
        ctx.hits.clear();
    }

    public static List<Entity> findCandidates(AxisAlignedBB area, Function<AxisAlignedBB, List<Entity>> search) {
        double overhang = AzHitboxes.maxOverhang();

        if (overhang <= 0) {
            return search.apply(area);
        }

        List<Entity> found = search.apply(area.grow(overhang));
        List<Entity> result = new ArrayList<>(found.size());

        for (Entity entity : found) {
            if (entity.getEntityBoundingBox().intersects(area)) {
                result.add(entity);
                continue;
            }

            AzHitboxState state = AzHitboxes.getLiveState(entity);

            if (state != null) {
                AxisAlignedBB bounds = state.worldBounds(entity);

                if (bounds != null && bounds.intersects(area)) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    public static AxisAlignedBB captureCandidate(Entity candidate, AxisAlignedBB boundingBox) {
        CONTEXT.get().candidate = candidate;
        return boundingBox;
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
    public static RayTraceResult clip(
        AxisAlignedBB inflated,
        Vec3d from,
        Vec3d to,
        Supplier<RayTraceResult> vanilla
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
            ctx.hits.remove(candidate);
            return keepEntityBox ? vanilla.get() : null;
        }

        Vec3d location = hit.get().location();

        if (keepEntityBox) {
            RayTraceResult boxHit = vanilla.get();

            if (
                boxHit != null && boxHit.hitVec != null && from.squareDistanceTo(boxHit.hitVec) < from.squareDistanceTo(
                    location
                )
            ) {
                ctx.hits.remove(candidate);
                return boxHit;
            }
        }

        ctx.hits.put(candidate, hit.get());
        return new RayTraceResult(location, EnumFacing.UP);
    }

    public static boolean contains(AxisAlignedBB inflated, Vec3d point, BooleanSupplier vanilla) {
        Context ctx = CONTEXT.get();
        AzHitboxState state = collidableState(ctx);

        if (state == null) {
            return vanilla.getAsBoolean();
        }

        Entity candidate = ctx.candidate;
        double inflate = inflationOf(candidate, inflated);
        AzOrientedBox[] boxes = state.worldBoxes(candidate);

        for (int i = 0; i < boxes.length; i++) {
            AzHitbox hitbox = state.set().get(i);

            if (boxes[i] != null && ctx.query.filter().test(hitbox) && boxes[i].inflate(inflate).contains(point)) {
                ctx.hits.put(candidate, new AzHitboxHit(candidate, hitbox, i, boxes[i], point));
                return true;
            }
        }

        return state.set().keepEntityBox() && vanilla.getAsBoolean();
    }

    @Nullable
    public static AzHitboxHit finishProjectileQuery(@Nullable Entity hitEntity, Entity projectile) {
        Context ctx = CONTEXT.get();
        ctx.candidate = null;

        if (hitEntity == null) {
            ctx.hits.clear();
            return null;
        }

        AzHitboxHit hit = ctx.hits.get(hitEntity);
        ctx.hits.clear();

        if (hit != null && !projectile.world.isRemote) {
            AzHitboxDamageTracker.recordProjectileHit(projectile, hit);
        }

        return hit;
    }

    public static void endQuery() {
        Context ctx = CONTEXT.get();
        ctx.candidate = null;
        ctx.hits.clear();
    }

    private static double inflationOf(Entity candidate, AxisAlignedBB inflated) {
        AxisAlignedBB box = candidate.getEntityBoundingBox();
        double inflate = ((inflated.maxX - inflated.minX) - (box.maxX - box.minX)) * 0.5;
        return Math.max(0, inflate);
    }
}
