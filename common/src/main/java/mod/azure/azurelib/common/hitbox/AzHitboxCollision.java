package mod.azure.azurelib.common.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        var ctx = CONTEXT.get();
        ctx.query = query;
        ctx.candidate = null;
        ctx.hits.clear();
    }

    public static List<Entity> findCandidates(AABB area, SearchFunction search) {
        var overhang = AzHitboxes.maxOverhang();

        if (overhang <= 0) {
            return search.search(area);
        }

        var found = search.search(area.inflate(overhang));
        var result = new ArrayList<Entity>(found.size());

        for (var entity : found) {
            if (entity.getBoundingBox().intersects(area)) {
                result.add(entity);
                continue;
            }

            var state = AzHitboxes.getLiveState(entity);

            if (state != null) {
                var bounds = state.worldBounds(entity);

                if (bounds != null && bounds.intersects(area)) {
                    result.add(entity);
                }
            }
        }

        return result;
    }

    @FunctionalInterface
    public interface SearchFunction {

        List<Entity> search(AABB area);
    }

    public static AABB captureCandidate(Entity candidate, AABB boundingBox) {
        CONTEXT.get().candidate = candidate;
        return boundingBox;
    }

    private static @Nullable AzHitboxState collidableState(Context ctx) {
        var candidate = ctx.candidate;

        if (candidate == null) {
            return null;
        }

        var state = AzHitboxes.getLiveState(candidate);

        if (state == null || !AzHitboxes.hasActive(state, ctx.query.filter())) {
            return null;
        }

        return state;
    }

    public static Optional<Vec3> clip(AABB inflated, Vec3 from, Vec3 to, Supplier<Optional<Vec3>> vanilla) {
        var ctx = CONTEXT.get();
        var state = collidableState(ctx);

        if (state == null) {
            return vanilla.get();
        }

        var candidate = ctx.candidate;
        var inflate = inflationOf(candidate, inflated);
        var hit = AzHitboxes.raycast(candidate, from, to, inflate, false, ctx.query.filter());
        var keepEntityBox = state.set().keepEntityBox();

        if (hit.isEmpty()) {
            ctx.hits.remove(candidate);
            return keepEntityBox ? vanilla.get() : Optional.empty();
        }

        var location = hit.get().location();

        if (keepEntityBox) {
            var boxHit = vanilla.get();

            if (boxHit.isPresent() && from.distanceToSqr(boxHit.get()) < from.distanceToSqr(location)) {
                ctx.hits.remove(candidate);
                return boxHit;
            }
        }

        ctx.hits.put(candidate, hit.get());
        return Optional.of(location);
    }

    public static boolean contains(AABB inflated, Vec3 point, BooleanSupplier vanilla) {
        var ctx = CONTEXT.get();
        var state = collidableState(ctx);

        if (state == null) {
            return vanilla.getAsBoolean();
        }

        var candidate = ctx.candidate;
        var inflate = inflationOf(candidate, inflated);
        var boxes = state.worldBoxes(candidate);

        for (int i = 0; i < boxes.length; i++) {
            var hitbox = state.set().get(i);

            if (boxes[i] != null && ctx.query.filter().test(hitbox) && boxes[i].inflate(inflate).contains(point)) {
                ctx.hits.put(candidate, new AzHitboxHit(candidate, hitbox, i, boxes[i], point));
                return true;
            }
        }

        return state.set().keepEntityBox() && vanilla.getAsBoolean();
    }

    public static @Nullable EntityHitResult finishProjectileQuery(@Nullable EntityHitResult result, Entity projectile) {
        var ctx = CONTEXT.get();
        ctx.candidate = null;

        if (result == null) {
            ctx.hits.clear();
            return null;
        }

        var hit = ctx.hits.get(result.getEntity());
        ctx.hits.clear();

        if (hit == null) {
            return result;
        }

        if (!projectile.level().isClientSide()) {
            AzHitboxDamageTracker.recordProjectileHit(projectile, hit);
        }

        return new EntityHitResult(result.getEntity(), hit.location());
    }

    private static double inflationOf(Entity candidate, AABB inflated) {
        var inflate = (inflated.getXsize() - candidate.getBoundingBox().getXsize()) * 0.5;
        return Math.max(0, inflate);
    }
}
