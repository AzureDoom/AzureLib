package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import mod.azure.azurelib.rewrite.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzModelRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * AzEntityModelRenderer is a class responsible for rendering animated 3D entity models in a pipeline-based rendering
 * setup. Extends the {@link AzModelRenderer} class and utilizes the {@link AzEntityRendererPipeline} to handle various
 * rendering tasks, such as applying model transformations and managing animated states in the rendering lifecycle. <br>
 *
 * @param <T> The type of entity that this renderer applies to, extends the {@link Entity} class.
 */
public class AzEntityModelRenderer<T extends Entity> extends AzModelRenderer<T> {

    protected final AzEntityRendererPipeline<T> entityRendererPipeline;

    public AzEntityModelRenderer(AzEntityRendererPipeline<T> entityRendererPipeline, AzLayerRenderer<T> layerRenderer) {
        super(entityRendererPipeline, layerRenderer);
        this.entityRendererPipeline = entityRendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link AzEntityRendererPipeline#preRender} has already been called by this stage, and
     * {@link AzEntityRendererPipeline#postRender} will be called directly after
     */
    @Override
    public void render(AzRendererPipelineContext<T> context, boolean isReRender) {
        T animatable = context.animatable();
        float partialTick = context.partialTick();
        MatrixStack poseStack = context.poseStack();

        poseStack.push();

        LivingEntity livingEntity = animatable instanceof LivingEntity ? (LivingEntity) animatable : null;

        boolean shouldSit = animatable.isPassenger() && (animatable.getRidingEntity() != null);
        float lerpBodyRot = livingEntity == null
            ? 0
            : MathHelper.rotLerp(
                partialTick,
                livingEntity.prevRenderYawOffset,
                livingEntity.renderYawOffset
            );
        float lerpHeadRot = livingEntity == null
            ? 0
            : MathHelper.rotLerp(
                partialTick,
                livingEntity.prevRotationYawHead,
                livingEntity.rotationYawHead
            );
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && animatable.getRidingEntity() instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) animatable.getRidingEntity();
            lerpBodyRot = MathHelper.rotLerp(
                partialTick,
                livingentity.prevRenderYawOffset,
                livingentity.renderYawOffset
            );
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = MathHelper.clamp(MathHelper.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;

            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        if (animatable.getPose() == Pose.SLEEPING && livingEntity != null) {
            Direction bedDirection = livingEntity.getBedDirection();

            if (bedDirection != null) {
                float eyePosOffset = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;

                poseStack.translate(
                    -bedDirection.getXOffset() * eyePosOffset,
                    0,
                    -bedDirection.getXOffset() * eyePosOffset
                );
            }
        }

        float nativeScale = livingEntity != null ? livingEntity.getRenderScale() : 1;
        float ageInTicks = animatable.ticksExisted + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!shouldSit && animatable.isAlive() && livingEntity != null) {
            limbSwingAmount = MathHelper.lerp(
                partialTick,
                livingEntity.prevLimbSwingAmount,
                livingEntity.limbSwingAmount
            );
            limbSwing = livingEntity.limbSwing - livingEntity.limbSwingAmount * (1 - partialTick);

            if (livingEntity.isChild()) {
                limbSwing *= 3f;
            }

            if (limbSwingAmount > 1f) {
                limbSwingAmount = 1f;
            }
        }

        if (!isReRender) {
            AzEntityAnimator<T> animator = entityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                animator.animate(animatable, context.partialTick());
            }
        }

        RenderUtils.copy(this.entityRendererPipeline.modelRenderTranslations, poseStack.getLast().getMatrix());

        if (!animatable.isInvisibleToPlayer(Minecraft.getInstance().player)) {
            super.render(context, isReRender);
        }

        poseStack.pop();
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        IVertexBuilder buffer = context.vertexConsumer();
        IRenderTypeBuffer bufferSource = context.multiBufferSource();
        T entity = context.animatable();
        MatrixStack poseStack = context.poseStack();
        RenderType renderType = context.renderType();

        poseStack.push();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.getLast().getMatrix());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                entityRendererPipeline.entityRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, entityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);

            worldState.translate(new Vector3f(entity.getPositionVec()));
            bone.setWorldSpaceMatrix(worldState);
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (!isReRender && buffer instanceof BufferBuilder && !((BufferBuilder) buffer).isDrawing) {
            context.setVertexConsumer(bufferSource.getBuffer(renderType));
        }

        renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);

        poseStack.pop();
    }

    /**
     * Applies rotation transformations to the renderer prior to render time to account for various entity states,
     * default scale of 1
     */
    protected void applyRotations(
        T animatable,
        MatrixStack poseStack,
        float ageInTicks,
        float rotationYaw,
        float partialTick
    ) {
        applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, 1);
    }

    /**
     * Applies rotation transformations to the renderer prior to render time to account for various entity states,
     * scalable
     */
    protected void applyRotations(
        T animatable,
        MatrixStack poseStack,
        float ageInTicks,
        float rotationYaw,
        float partialTick,
        float nativeScale
    ) {
        if (isShaking(animatable)) {
            rotationYaw += (float) (Math.cos(animatable.ticksExisted * 3.25d) * Math.PI * 0.4d);
        }

        if (animatable.getPose() != Pose.SLEEPING) {
            poseStack.rotate(Vector3f.YP.rotationDegrees(180f - rotationYaw));
        }

        if (animatable instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) animatable;
            AzEntityRendererConfig<T> config = entityRendererPipeline.getRenderer().config();
            float deathMaxRotation = config.getDeathMaxRotation(animatable);

            if (livingEntity.deathTime > 0) {
                float deathRotation = (livingEntity.deathTime + partialTick - 1f) / 20f * 1.6f;

                poseStack.rotate(
                    Vector3f.ZP.rotationDegrees(Math.min(MathHelper.sqrt(deathRotation), 1) * deathMaxRotation)
                );
            } else if (livingEntity.isSpinAttacking()) {
                poseStack.rotate(Vector3f.XP.rotationDegrees(-90f - livingEntity.rotationPitch));
                poseStack.rotate(Vector3f.YP.rotationDegrees((livingEntity.ticksExisted + partialTick) * -75f));
            } else if (animatable.getPose() == Pose.SLEEPING) {
                Direction bedOrientation = livingEntity.getBedDirection();

                poseStack.rotate(
                    Vector3f.YP.rotationDegrees(
                        bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw
                    )
                );
                poseStack.rotate(Vector3f.ZP.rotationDegrees(deathMaxRotation));
                poseStack.rotate(Vector3f.YP.rotationDegrees(270f));
            } else if (isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (animatable.getHeight() + 0.1f) / nativeScale, 0);
                poseStack.rotate(Vector3f.ZP.rotationDegrees(180f));
            }
        }
    }

    public boolean isEntityUpsideDown(LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity || livingEntity.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(livingEntity.getName().getString());
            if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
                return !(livingEntity instanceof PlayerEntity) || ((PlayerEntity) livingEntity).isWearing(
                    PlayerModelPart.CAPE
                );
            }
        }
        return false;
    }

    public boolean isShaking(T entity) {
        return false;
    }
}
