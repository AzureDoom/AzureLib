package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

public final class AzHitboxes {

    private static final double MAX_OVERHANG = 32;

    private static volatile double maxOverhang;

    private AzHitboxes() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static AzHitboxState getState(Entity entity) {
        if (!(entity instanceof AzHitboxHolder)) {
            return null;
        }

        return ((AzHitboxStateAccessor) entity).azurelib$getHitboxState();
    }

    @Nullable
    public static AzHitboxState getOrCreateState(Entity entity) {
        if (!(entity instanceof AzHitboxHolder)) {
            return null;
        }

        AzHitboxSet set = ((AzHitboxHolder) entity).getHitboxSet();

        if (set == null || set.isEmpty()) {
            return null;
        }

        AzHitboxStateAccessor accessor = (AzHitboxStateAccessor) entity;
        AzHitboxState state = accessor.azurelib$getHitboxState();

        if (state == null || state.set() != set) {
            state = new AzHitboxState(set);
            accessor.azurelib$setHitboxState(state);
        }

        return state;
    }

    @Nullable
    public static AzHitboxState getLiveState(Entity entity) {
        AzHitboxState state = getState(entity);

        if (state == null || state.set() != ((AzHitboxHolder) entity).getHitboxSet()) {
            return null;
        }

        return state.isLive(Mc.time(Mc.world(entity))) ? state : null;
    }

    public static boolean hasLivePose(Entity entity) {
        return getLiveState(entity) != null;
    }

    @Nullable
    public static AzOrientedBox getWorldBox(Entity entity, String name) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return null;
        }

        int index = state.set().indexOf(name);
        return index < 0 ? null : state.worldBoxes(entity)[index];
    }

    public static List<AzHitboxHit> getActiveHitboxes(Entity entity) {
        return getActiveHitboxes(entity, hitbox -> true);
    }

    public static List<AzHitboxHit> getActiveHitboxes(Entity entity, Predicate<AzHitbox> filter) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return Collections.emptyList();
        }

        AzOrientedBox[] boxes = state.worldBoxes(entity);
        List<AzHitboxHit> result = new ArrayList<>(boxes.length);

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && filter.test(state.set().get(i))) {
                result.add(new AzHitboxHit(entity, state.set().get(i), i, boxes[i], boxes[i].center()));
            }
        }

        return result;
    }

    public static Optional<AzHitboxHit> raycast(Entity entity, Vec3d from, Vec3d to) {
        return raycast(entity, from, to, hitbox -> true);
    }

    public static Optional<AzHitboxHit> raycast(Entity entity, Vec3d from, Vec3d to, Predicate<AzHitbox> filter) {
        return raycast(entity, from, to, 0, true, filter);
    }

    static Optional<AzHitboxHit> raycast(
        Entity entity,
        Vec3d from,
        Vec3d to,
        double inflate,
        boolean hitIfInside,
        Predicate<AzHitbox> filter
    ) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return Optional.empty();
        }

        AzOrientedBox[] boxes = state.worldBoxes(entity);
        double best = Double.MAX_VALUE;
        int bestIndex = -1;
        AzOrientedBox bestBox = null;

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] == null || !filter.test(state.set().get(i))) {
                continue;
            }

            AzOrientedBox box = boxes[i].inflate(inflate);
            double t = box.clipFraction(from, to, hitIfInside);

            if (t >= 0 && t < best) {
                best = t;
                bestIndex = i;
                bestBox = box;
            }
        }

        if (bestIndex < 0) {
            return Optional.empty();
        }

        Vec3d location = AzHitboxMath.lerp(from, to, best);
        return Optional.of(new AzHitboxHit(entity, state.set().get(bestIndex), bestIndex, bestBox, location));
    }

    public static List<AzHitboxHit> getIntersecting(Entity entity, Box area) {
        return getIntersecting(entity, area, hitbox -> true);
    }

    public static List<AzHitboxHit> getIntersecting(Entity entity, Box area, Predicate<AzHitbox> filter) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return Collections.emptyList();
        }

        Box bounds = state.worldBounds(entity);

        if (bounds == null || !AzHitboxMath.intersects(bounds, area)) {
            return Collections.emptyList();
        }

        AzOrientedBox[] boxes = state.worldBoxes(entity);
        AzOrientedBox areaBox = AzOrientedBox.of(area);
        List<AzHitboxHit> result = new ArrayList<>();

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && filter.test(state.set().get(i)) && boxes[i].intersects(areaBox)) {
                result.add(new AzHitboxHit(entity, state.set().get(i), i, boxes[i], boxes[i].center()));
            }
        }

        return result;
    }

    public static Box getCullingBox(Entity entity, Box original) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return original;
        }

        Box bounds = state.worldBounds(entity);
        return bounds == null ? original : AzHitboxMath.union(original, bounds);
    }

    public static Box getInteractionBox(Entity entity, Box original) {
        AzHitboxState state = getLiveState(entity);

        if (state == null) {
            return original;
        }

        AzOrientedBox[] boxes = state.worldBoxes(entity);
        Box result = original;

        for (int i = 0; i < boxes.length; i++) {
            if (boxes[i] != null && state.set().get(i).role().isPickable()) {
                result = AzHitboxMath.union(result, boxes[i].bounds());
            }
        }

        return result;
    }

    @Nullable
    public static AzHitboxHit getLastHit(Entity entity) {
        AzHitboxState state = getState(entity);

        if (state == null || state.lastHit == null) {
            return null;
        }

        return Mc.time(Mc.world(entity)) - state.lastHitTime <= 0 ? state.lastHit : null;
    }

    public static float bodyYaw(Entity entity) {
        return entity instanceof MobEntity ? Mc.bodyYaw((MobEntity) entity) : Mc.yaw(entity);
    }

    /** Beta clamps the crosshair's entity reach to 3 blocks (there is no creative mode to widen it). */
    public static double attackReach(PlayerEntity player) {
        return 3.0;
    }

    public static double maxOverhang() {
        return maxOverhang;
    }

    static boolean hasActive(AzHitboxState state, Predicate<AzHitbox> filter) {
        for (int i = 0; i < state.size(); i++) {
            if (state.isActive(i) && filter.test(state.set().get(i))) {
                return true;
            }
        }

        return false;
    }

    static void noteOverhang(Box entityBox, Box hitboxBounds) {
        double overhang = Math.max(
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
