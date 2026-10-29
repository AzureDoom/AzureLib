package mod.azure.azurelib.animation.pose.ik;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import mod.azure.azurelib.animation.pose.AzEntityModelSpace;

/**
 * Common {@link AzIkTarget}s. World-space targets are converted with an {@link AzEntityModelSpace}; pass
 * {@link AzEntityModelSpace#scaled} if your renderer scales the model.
 */
public final class AzIkTargets {

    private AzIkTargets() {}

    public static <T> AzIkTarget<T> model(float x, float y, float z) {
        return (pose, effector, dest) -> {
            dest.set(-x / 16f, y / 16f, z / 16f);
            return true;
        };
    }

    public static <T extends Entity> AzIkTarget<T> world(BiFunction<T, Float, Vec3> position) {
        return world(position, AzEntityModelSpace.DEFAULT);
    }

    public static <T extends Entity> AzIkTarget<T> world(
        BiFunction<T, Float, Vec3> position,
        AzEntityModelSpace space
    ) {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(space, "space");

        return (pose, effector, dest) -> {
            T entity = pose.animatable();
            Vec3 world = position.apply(entity, pose.partialTick());

            if (world == null) {
                return false;
            }

            space.worldToModel(entity, pose.partialTick(), world.x, world.y, world.z, dest);
            return true;
        };
    }

    /**
     * The eyes of another entity, e.g. the animatable's attack target, for look-at. Returning {@code null} skips
     * solving.
     */
    public static <T extends Entity> AzIkTarget<T> entityEyes(Function<T, Entity> lookTarget) {
        Objects.requireNonNull(lookTarget, "lookTarget");
        return world((entity, partialTick) -> {
            Entity other = lookTarget.apply(entity);
            return other == null || other.isRemoved() ? null : other.getEyePosition(partialTick);
        });
    }

    /**
     * Foot placement: keeps the effector's animated height above the ground, but measured from the ground actually
     * under it instead of from the entity's feet, so feet step onto slopes, stairs and ledges. Searches straight down
     * from {@code maxStepUp} blocks above the animated foot to {@code maxStepDown} blocks below it, and skips solving
     * (leaving the leg as animated) when nothing is found or the entity is not on the ground.
     * <p>
     * Feet can only be lowered as far as the leg reaches; lowering the body to match is up to you (for example a pose
     * modifier that translates the root bone by the lower foot's offset).
     * </p>
     */
    public static <T extends Entity> AzIkTarget<T> ground(float maxStepUp, float maxStepDown) {
        return ground(maxStepUp, maxStepDown, AzEntityModelSpace.DEFAULT);
    }

    /** {@link #ground(float, float)} for renderers that scale the model. */
    public static <T extends Entity> AzIkTarget<T> ground(
        float maxStepUp,
        float maxStepDown,
        AzEntityModelSpace space
    ) {
        Objects.requireNonNull(space, "space");

        return (pose, effector, dest) -> {
            T entity = pose.animatable();

            if (!entity.isOnGround()) {
                return false;
            }

            float partialTick = pose.partialTick();
            Vec3 foot = space.modelToWorld(entity, partialTick, effector);
            Vec3 from = new Vec3(foot.x, foot.y + maxStepUp, foot.z);
            Vec3 to = new Vec3(foot.x, foot.y - maxStepDown, foot.z);
            BlockHitResult hit = entity.level
                .clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));

            if (hit.getType() != HitResult.Type.BLOCK) {
                return false;
            }

            // How far the ground under this foot is from the ground the entity stands on.
            double groundOffset = hit.getLocation().y - entity.getPosition(partialTick).y;
            space.worldToModel(entity, partialTick, foot.x, foot.y + groundOffset, foot.z, dest);
            return true;
        };
    }
}
