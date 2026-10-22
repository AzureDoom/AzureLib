package mod.azure.azurelib.hitbox;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class AzHitboxes {

    private static final double MAX_OVERHANG = 32;

    private static volatile double maxOverhang;

    private AzHitboxes() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable AzHitboxState getState(Entity entity) {
        if (!(entity instanceof AzHitboxHolder)) {
            return null;
        }

        return ((AzHitboxStateAccessor) entity).azurelib$getHitboxState();
    }

    public static @Nullable AzHitboxState getOrCreateState(Entity entity) {
        if (!(entity instanceof AzHitboxHolder holder)) {
            return null;
        }

        var set = holder.getHitboxSet();

        if (set == null || set.isEmpty()) {
            return null;
        }

        var accessor = (AzHitboxStateAccessor) entity;
        var state = accessor.azurelib$getHitboxState();

        if (state == null || state.set() != set) {
            state = new AzHitboxState(set);
            accessor.azurelib$setHitboxState(state);
        }

        return state;
    }

    public static @Nullable AzHitboxState getLiveState(Entity entity) {
        var state = getState(entity);

        if (state == null || state.set() != ((AzHitboxHolder) entity).getHitboxSet()) {
            return null;
        }

        return state.isLive(entity.level().getGameTime()) ? state : null;
    }

    public static boolean hasLivePose(Entity entity) {
        return getLiveState(entity) != null;
    }

    public static @Nullable AzOrientedBox getWorldBox(Entity entity, String name) {
        var state = getLiveState(entity);

        if (state == null) {
            return null;
        }

        var index = state.set().indexOf(name);
        return index < 0 ? null : state.worldBoxes(entity)[index];
    }

    public static List<AzHitboxHit> getActiveHitboxes(Entity entity) {
        return getActiveHitboxes(entity, hitbox -> true);
    }

    public static List<AzHitboxHit> getActiveHitboxes(Entity entity, Predicate<AzHitbox> filter) {
        var state = getLiveState(entity);

        if (state == null) {
            return List.of();
        }

        var boxes = state.worldBoxes(entity);
        var result = new ArrayList<AzHitboxHit>(boxes.length);

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && filter.test(state.set().get(i))) {
                result.add(new AzHitboxHit(entity, state.set().get(i), i, boxes[i], boxes[i].center()));
            }
        }

        return result;
    }

    public static Optional<AzHitboxHit> raycast(Entity entity, Vec3 from, Vec3 to) {
        return raycast(entity, from, to, hitbox -> true);
    }

    public static Optional<AzHitboxHit> raycast(Entity entity, Vec3 from, Vec3 to, Predicate<AzHitbox> filter) {
        return raycast(entity, from, to, 0, true, filter);
    }

    static Optional<AzHitboxHit> raycast(
        Entity entity,
        Vec3 from,
        Vec3 to,
        double inflate,
        boolean hitIfInside,
        Predicate<AzHitbox> filter
    ) {
        var state = getLiveState(entity);

        if (state == null) {
            return Optional.empty();
        }

        var boxes = state.worldBoxes(entity);
        var best = Double.MAX_VALUE;
        var bestIndex = -1;
        AzOrientedBox bestBox = null;

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null || !filter.test(state.set().get(i))) {
                continue;
            }

            var box = boxes[i].inflate(inflate);
            var t = box.clipFraction(from, to, hitIfInside);

            if (t >= 0 && t < best) {
                best = t;
                bestIndex = i;
                bestBox = box;
            }
        }

        if (bestIndex < 0) {
            return Optional.empty();
        }

        var location = from.add(to.subtract(from).scale(best));
        return Optional.of(new AzHitboxHit(entity, state.set().get(bestIndex), bestIndex, bestBox, location));
    }

    public static List<AzHitboxHit> getIntersecting(Entity entity, AABB area) {
        return getIntersecting(entity, area, hitbox -> true);
    }

    public static List<AzHitboxHit> getIntersecting(Entity entity, AABB area, Predicate<AzHitbox> filter) {
        var state = getLiveState(entity);

        if (state == null) {
            return List.of();
        }

        var bounds = state.worldBounds(entity);

        if (bounds == null || !bounds.intersects(area)) {
            return List.of();
        }

        var boxes = state.worldBoxes(entity);
        var areaBox = AzOrientedBox.of(area);
        var result = new ArrayList<AzHitboxHit>();

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && filter.test(state.set().get(i)) && boxes[i].intersects(areaBox)) {
                result.add(new AzHitboxHit(entity, state.set().get(i), i, boxes[i], boxes[i].center()));
            }
        }

        return result;
    }

    public static AABB getCullingBox(Entity entity, AABB original) {
        var state = getLiveState(entity);

        if (state == null) {
            return original;
        }

        var bounds = state.worldBounds(entity);
        return bounds == null ? original : original.minmax(bounds);
    }

    public static AABB getInteractionBox(Entity entity, AABB original) {
        var state = getLiveState(entity);

        if (state == null) {
            return original;
        }

        var boxes = state.worldBoxes(entity);
        var result = original;

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && state.set().get(i).role().isPickable()) {
                result = result.minmax(boxes[i].bounds());
            }
        }

        return result;
    }

    static boolean hasActive(AzHitboxState state, Predicate<AzHitbox> filter) {
        for (int i = 0; i < state.size(); i++) {
            if (state.isActive(i) && filter.test(state.set().get(i))) {
                return true;
            }
        }

        return false;
    }

    public static @Nullable AzHitboxHit getLastHit(Entity entity) {
        var state = getState(entity);

        if (state == null || state.lastHit == null) {
            return null;
        }

        return entity.level().getGameTime() - state.lastHitTime <= 0 ? state.lastHit : null;
    }

    public static float bodyYaw(Entity entity) {
        return entity instanceof LivingEntity living ? living.yBodyRot : entity.getYRot();
    }

    public static double maxOverhang() {
        return maxOverhang;
    }

    static void noteOverhang(AABB entityBox, AABB hitboxBounds) {
        var overhang = Math.max(
            Math.max(entityBox.minX - hitboxBounds.minX, hitboxBounds.maxX - entityBox.maxX),
            Math.max(
                Math.max(entityBox.minY - hitboxBounds.minY, hitboxBounds.maxY - entityBox.maxY),
                Math.max(entityBox.minZ - hitboxBounds.minZ, hitboxBounds.maxZ - entityBox.maxZ)
            )
        );

        if (overhang > maxOverhang) {
            maxOverhang = Math.min(MAX_OVERHANG, overhang);
        }
    }
}
