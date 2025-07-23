package mod.azure.azurelib.rewrite.animation.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorConfig;
import mod.azure.azurelib.util.RenderUtils;

/**
 * The {@code AzEntityAnimator} class extends {@link AzAnimator} to provide specialized animation management for
 * entities. This abstract class is designed to handle various animation-related requirements for entities in a game
 * framework, including the application of MoLang queries specific to entity-related properties such as position,
 * health, and motion state.
 *
 * @param <T> The type of entity this animator is designed to manage.
 */
public abstract class AzEntityAnimator<T extends Entity> extends AzAnimator<T> {

    protected AzEntityAnimator() {
        super();
    }

    protected AzEntityAnimator(AzAnimatorConfig config) {
        super(config);
    }

    /**
     * Applies MoLang queries specific to an entity in the animation system. These queries provide contextual
     * information about the entity's state and environment, such as its position, health, movement, and interaction
     * with the world. The method extends the baseline queries defined in the superclass with additional entity-specific
     * properties, particularly for living entities.
     *
     * @param entity   The entity for which the MoLang queries are being applied.
     * @param animTime The current animation time, in seconds, used for time-dependent queries.
     */
    @Override
    protected void applyMolangQueries(T entity, double animTime, float partialTicks) {
        super.applyMolangQueries(entity, animTime, partialTicks);

        MolangParser parser = MolangParser.INSTANCE;
        Minecraft minecraft = Minecraft.getInstance();

        parser.setMemoizedValue(
            MolangQueries.DISTANCE_FROM_CAMERA,
            () -> minecraft.gameRenderer.getActiveRenderInfo().getProjectedView().distanceTo(entity.getPositionVec())
        );
        parser.setMemoizedValue(MolangQueries.IN_AIR, () -> RenderUtils.booleanToFloat(!entity.onGround));
        parser.setMemoizedValue(MolangQueries.IS_ON_GROUND, () -> RenderUtils.booleanToFloat(entity.onGround));
        parser.setMemoizedValue(MolangQueries.IS_IN_WATER, () -> RenderUtils.booleanToFloat(entity.isInWater()));
        parser.setMemoizedValue(
            MolangQueries.IS_IN_WATER_OR_RAIN,
            () -> RenderUtils.booleanToFloat(entity.isInWaterRainOrBubbleColumn())
        );
        parser.setMemoizedValue(MolangQueries.IS_ON_FIRE, () -> RenderUtils.booleanToFloat(entity.isBurning()));

        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            parser.setMemoizedValue(
                MolangQueries.IS_BLOCKING,
                () -> RenderUtils.booleanToFloat(livingEntity.isActiveItemStackBlocking())
            );
            parser.setMemoizedValue(
                MolangQueries.IS_USING_ITEM,
                () -> RenderUtils.booleanToFloat(livingEntity.isHandActive())
            );
            parser.setMemoizedValue(MolangQueries.HEALTH, livingEntity::getHealth);
            parser.setMemoizedValue(MolangQueries.MAX_HEALTH, livingEntity::getMaxHealth);
            parser.setMemoizedValue(MolangQueries.GROUND_SPEED, () -> {
                Vec3d velocity = livingEntity.getMotion();
                return Math.sqrt((float) ((velocity.x * velocity.x) + (velocity.z * velocity.z)));
            });
            parser.setMemoizedValue(
                MolangQueries.YAW_SPEED,
                () -> livingEntity.rotationYaw - livingEntity.prevRotationYaw
            );
            parser.setValue(
                MolangQueries.HEAD_YAW,
                () -> livingEntity.getYaw(partialTicks) - MathHelper.lerp(
                    partialTicks,
                    livingEntity.prevRenderYawOffset,
                    livingEntity.renderYawOffset
                )
            );
            parser.setValue(MolangQueries.HEAD_PITCH, () -> livingEntity.getPitch(partialTicks));
            parser.setValue(
                MolangQueries.HURT_TIME,
                () -> livingEntity.hurtTime == 0 ? 0 : livingEntity.hurtTime - partialTicks
            );
            parser.setValue(MolangQueries.IS_BABY, () -> RenderUtils.booleanToFloat(livingEntity.isChild()));
            parser.setValue(MolangQueries.LIMB_SWING, () -> livingEntity.limbSwing);
            parser.setValue(
                MolangQueries.LIMB_SWING_AMOUNT,
                () -> MathHelper.lerp(partialTicks, livingEntity.prevLimbSwingAmount, livingEntity.limbSwingAmount)
            );
        }
    }
}
