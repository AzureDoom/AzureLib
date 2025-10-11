package mod.azure.azurelib.animation.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorConfig;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
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

        var parser = MolangParser.INSTANCE;
        var minecraft = Minecraft.getInstance();

        parser.setMemoizedValue(
            MolangQueries.DISTANCE_FROM_CAMERA,
            () -> minecraft.gameRenderer.getMainCamera().getPosition().distanceTo(entity.position())
        );
        parser.setMemoizedValue(MolangQueries.IN_AIR, () -> RenderUtils.booleanToFloat(!entity.onGround()));
        parser.setMemoizedValue(MolangQueries.IS_ON_GROUND, () -> RenderUtils.booleanToFloat(entity.onGround()));
        parser.setMemoizedValue(MolangQueries.IS_IN_WATER, () -> RenderUtils.booleanToFloat(entity.isInWater()));
        parser.setMemoizedValue(
            MolangQueries.IS_IN_WATER_OR_RAIN,
            () -> RenderUtils.booleanToFloat(entity.isInWaterOrRain())
        );
        parser.setMemoizedValue(MolangQueries.IS_ON_FIRE, () -> RenderUtils.booleanToFloat(entity.isOnFire()));

        if (entity instanceof LivingEntity livingEntity) {
            parser.setMemoizedValue(
                MolangQueries.IS_BLOCKING,
                () -> RenderUtils.booleanToFloat(livingEntity.isBlocking())
            );
            parser.setMemoizedValue(
                MolangQueries.IS_USING_ITEM,
                () -> RenderUtils.booleanToFloat(livingEntity.isUsingItem())
            );
            parser.setMemoizedValue(MolangQueries.HEALTH, livingEntity::getHealth);
            parser.setMemoizedValue(MolangQueries.MAX_HEALTH, livingEntity::getMaxHealth);
            parser.setMemoizedValue(MolangQueries.GROUND_SPEED, () -> {
                var velocity = livingEntity.getDeltaMovement();
                return Mth.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
            });
            parser.setMemoizedValue(MolangQueries.YAW_SPEED, () -> livingEntity.getYRot() - livingEntity.yRotO);
            parser.setValue(
                MolangQueries.HEAD_YAW,
                () -> livingEntity.getViewYRot(partialTicks) - Mth.lerp(
                    partialTicks,
                    livingEntity.yBodyRotO,
                    livingEntity.yBodyRot
                )
            );
            parser.setValue(MolangQueries.HEAD_PITCH, () -> livingEntity.getViewXRot(partialTicks));
            parser.setValue(
                MolangQueries.HURT_TIME,
                () -> livingEntity.hurtTime == 0 ? 0 : livingEntity.hurtTime - partialTicks
            );
            parser.setValue(MolangQueries.IS_BABY, () -> RenderUtils.booleanToFloat(livingEntity.isBaby()));
            parser.setValue(MolangQueries.LIMB_SWING, livingEntity.walkAnimation::position);
            parser.setValue(MolangQueries.LIMB_SWING_AMOUNT, () -> livingEntity.walkAnimation.speed(partialTicks));
        }
    }
}
