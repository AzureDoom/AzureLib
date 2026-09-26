package mod.azure.azurelib.animation.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * The {@code AzEntityAnimator} class extends {@link AzAnimator} to provide specialized animation management for
 * entities. This abstract class is designed to handle various animation-related requirements for entities in a game
 * framework, including the application of MoLang queries specific to entity-related properties such as position,
 * health, and motion state.
 *
 * @param <T> The type of entity this animator is designed to manage.
 */
public abstract class AzEntityAnimator<T extends Entity> extends AzAnimator<UUID, T> {

    private static final MolangVariableRef DISTANCE_FROM_CAMERA_REF = new MolangVariableRef(
        MolangQueries.DISTANCE_FROM_CAMERA
    );

    private static final MolangVariableRef IN_AIR_REF = new MolangVariableRef(MolangQueries.IN_AIR);

    private static final MolangVariableRef IS_ON_GROUND_REF = new MolangVariableRef(MolangQueries.IS_ON_GROUND);

    private static final MolangVariableRef IS_IN_WATER_REF = new MolangVariableRef(MolangQueries.IS_IN_WATER);

    private static final MolangVariableRef IS_IN_WATER_OR_RAIN_REF = new MolangVariableRef(
        MolangQueries.IS_IN_WATER_OR_RAIN
    );

    private static final MolangVariableRef IS_BLOCKING_REF = new MolangVariableRef(MolangQueries.IS_BLOCKING);

    private static final MolangVariableRef IS_USING_ITEM_REF = new MolangVariableRef(MolangQueries.IS_USING_ITEM);

    private static final MolangVariableRef HEALTH_REF = new MolangVariableRef(MolangQueries.HEALTH);

    private static final MolangVariableRef MAX_HEALTH_REF = new MolangVariableRef(MolangQueries.MAX_HEALTH);

    private static final MolangVariableRef GROUND_SPEED_REF = new MolangVariableRef(MolangQueries.GROUND_SPEED);

    private static final MolangVariableRef YAW_SPEED_REF = new MolangVariableRef(MolangQueries.YAW_SPEED);

    private static final MolangVariableRef HEAD_YAW_REF = new MolangVariableRef(MolangQueries.HEAD_YAW);

    private static final MolangVariableRef HEAD_PITCH_REF = new MolangVariableRef(MolangQueries.HEAD_PITCH);

    private static final MolangVariableRef HURT_TIME_REF = new MolangVariableRef(MolangQueries.HURT_TIME);

    private static final MolangVariableRef IS_BABY_REF = new MolangVariableRef(MolangQueries.IS_BABY);

    private static final MolangVariableRef LIMB_SWING_REF = new MolangVariableRef(MolangQueries.LIMB_SWING);

    private static final MolangVariableRef LIMB_SWING_AMOUNT_REF = new MolangVariableRef(
        MolangQueries.LIMB_SWING_AMOUNT
    );

    /*
     * The entity currently being animated. The query suppliers below are created once and read these fields, rather
     * than capturing the entity in new lambdas every frame, so binding the queries allocates nothing. The fields are
     * set in applyMolangQueries and all Molang evaluation happens later in the same animate() call. The animator
     * already holds its animatable through its current context, so this adds no new retention.
     */
    private T currentEntity;

    /** {@link #currentEntity} as a LivingEntity, or null when it is not one. */
    private LivingEntity currentLivingEntity;

    private float currentPartialTicks;

    private final DoubleSupplier distanceFromCameraSupplier = () -> Minecraft.getInstance().gameRenderer
        .getMainCamera()
        .getPosition()
        .distanceTo(currentEntity.position());

    private final DoubleSupplier inAirSupplier = () -> RenderUtils.booleanToFloat(!currentEntity.isOnGround());

    private final DoubleSupplier isOnGroundSupplier = () -> RenderUtils.booleanToFloat(currentEntity.isOnGround());

    private final DoubleSupplier isInWaterSupplier = () -> RenderUtils.booleanToFloat(currentEntity.isInWater());

    private final DoubleSupplier isInWaterOrRainSupplier = () -> RenderUtils.booleanToFloat(
        currentEntity.isInWaterOrRain()
    );

    private final DoubleSupplier isBlockingSupplier = () -> currentLivingEntity == null
        ? 0
        : RenderUtils.booleanToFloat(currentLivingEntity.isBlocking());

    private final DoubleSupplier isUsingItemSupplier = () -> currentLivingEntity == null
        ? 0
        : RenderUtils.booleanToFloat(currentLivingEntity.isUsingItem());

    private final DoubleSupplier healthSupplier = () -> currentLivingEntity == null
        ? 0
        : currentLivingEntity.getHealth();

    private final DoubleSupplier maxHealthSupplier = () -> currentLivingEntity == null
        ? 0
        : currentLivingEntity.getMaxHealth();

    private final DoubleSupplier groundSpeedSupplier = () -> {
        if (currentLivingEntity == null)
            return 0;

        var velocity = currentLivingEntity.getDeltaMovement();
        return Mth.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
    };

    private final DoubleSupplier yawSpeedSupplier = () -> currentLivingEntity == null
        ? 0
        : currentLivingEntity.getYRot() - currentLivingEntity.yRotO;

    private final DoubleSupplier headYawSupplier = () -> {
        if (currentLivingEntity == null)
            return 0;

        return currentLivingEntity.getViewYRot(currentPartialTicks) - Mth.lerp(
            currentPartialTicks,
            currentLivingEntity.yBodyRotO,
            currentLivingEntity.yBodyRot
        );
    };

    private final DoubleSupplier headPitchSupplier = () -> currentLivingEntity == null
        ? 0
        : currentLivingEntity.getViewXRot(currentPartialTicks);

    private final DoubleSupplier hurtTimeSupplier = () -> currentLivingEntity == null
        || currentLivingEntity.hurtTime == 0
            ? 0
            : currentLivingEntity.hurtTime - currentPartialTicks;

    private final DoubleSupplier isBabySupplier = () -> currentLivingEntity == null
        ? 0
        : RenderUtils.booleanToFloat(currentLivingEntity.isBaby());

    private final DoubleSupplier limbSwingSupplier = () -> currentLivingEntity == null
        ? 0
        : currentLivingEntity.animationPosition;

    private final DoubleSupplier limbSwingAmountSupplier = () -> currentLivingEntity == null
        ? 0
        : Mth.lerp(currentPartialTicks, currentLivingEntity.animationSpeedOld, currentLivingEntity.animationSpeed);

    protected AzEntityAnimator() {
        super();
    }

    protected AzEntityAnimator(AzAnimatorConfig config) {
        super(config);
    }

    /**
     * Applies MoLang queries to the given entity, setting various parameters related to its state and properties. This
     * method customizes animation behavior by populating MoLang queries with entity-specific data such as position,
     * health, motion state, and environmental conditions.
     *
     * @param entity       The entity being animated. It can be of any type extending {@code Entity}.
     * @param animTime     The time in seconds related to the current animation cycle.
     * @param partialTicks A partial tick value used to interpolate animations smoothly.
     */
    @Override
    protected void applyMolangQueries(T entity, double animTime, float partialTicks) {
        super.applyMolangQueries(entity, animTime, partialTicks);

        this.currentEntity = entity;
        this.currentLivingEntity = entity instanceof LivingEntity livingEntity ? livingEntity : null;
        this.currentPartialTicks = partialTicks;

        DISTANCE_FROM_CAMERA_REF.setMemoized(distanceFromCameraSupplier);
        IN_AIR_REF.setMemoized(inAirSupplier);
        IS_ON_GROUND_REF.setMemoized(isOnGroundSupplier);
        IS_IN_WATER_REF.setMemoized(isInWaterSupplier);
        IS_IN_WATER_OR_RAIN_REF.setMemoized(isInWaterOrRainSupplier);

        IS_BLOCKING_REF.setMemoized(isBlockingSupplier);
        IS_USING_ITEM_REF.setMemoized(isUsingItemSupplier);
        HEALTH_REF.setMemoized(healthSupplier);
        MAX_HEALTH_REF.setMemoized(maxHealthSupplier);
        GROUND_SPEED_REF.setMemoized(groundSpeedSupplier);
        YAW_SPEED_REF.setMemoized(yawSpeedSupplier);
        HEAD_YAW_REF.set(headYawSupplier);
        HEAD_PITCH_REF.set(headPitchSupplier);
        HURT_TIME_REF.set(hurtTimeSupplier);
        IS_BABY_REF.set(isBabySupplier);
        LIMB_SWING_REF.set(limbSwingSupplier);
        LIMB_SWING_AMOUNT_REF.set(limbSwingAmountSupplier);
    }
}
