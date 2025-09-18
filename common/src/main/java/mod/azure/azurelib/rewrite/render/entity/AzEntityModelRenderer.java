package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

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
        var animatable = context.animatable();
        var partialTick = context.partialTick();
        var poseStack = context.poseStack();

        poseStack.pushPose();

        LivingEntity livingEntity = animatable instanceof LivingEntity entity ? entity : null;

        boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null);
        float lerpBodyRot = livingEntity == null
            ? 0
            : Mth.rotLerp(
                partialTick,
                livingEntity.yBodyRotO,
                livingEntity.yBodyRot
            );
        float lerpHeadRot = livingEntity == null
            ? 0
            : Mth.rotLerp(
                partialTick,
                livingEntity.yHeadRotO,
                livingEntity.yHeadRot
            );
        float netHeadYaw = lerpHeadRot - lerpBodyRot;

        if (shouldSit && animatable.getVehicle() instanceof LivingEntity livingentity) {
            lerpBodyRot = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
            netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;

            netHeadYaw = lerpHeadRot - lerpBodyRot;
        }

        if (animatable.getPose() == Pose.SLEEPING && livingEntity != null) {
            Direction bedDirection = livingEntity.getBedOrientation();

            if (bedDirection != null) {
                float eyePosOffset = livingEntity.getEyeHeight(Pose.STANDING) - 0.1F;

                poseStack.translate(
                    -bedDirection.getStepX() * eyePosOffset,
                    0,
                    -bedDirection.getStepZ() * eyePosOffset
                );
            }
        }

        float nativeScale = livingEntity != null ? livingEntity.getScale() : 1;
        float ageInTicks = animatable.tickCount + partialTick;
        float limbSwingAmount = 0;
        float limbSwing = 0;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!shouldSit && animatable.isAlive() && livingEntity != null) {
            limbSwingAmount = Mth.lerp(
                partialTick,
                livingEntity.animationSpeedOld,
                livingEntity.animationSpeed
            );
            limbSwing = livingEntity.animationPosition - livingEntity.animationSpeed * (1 - partialTick);

            if (livingEntity.isBaby()) {
                limbSwing *= 3f;
            }

            if (limbSwingAmount > 1f) {
                limbSwingAmount = 1f;
            }
        }

        if (!isReRender) {
            var animator = entityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                handleAnimation(animator, animatable, context.partialTick());
            }
        }

        entityRendererPipeline.modelRenderTranslations.load(poseStack.last().pose());

        if (!animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            super.render(context, isReRender);
        }

        poseStack.popPose();
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var entity = context.animatable();
        var poseStack = context.poseStack();
        var renderType = context.renderType();

        poseStack.pushPose();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                entityRendererPipeline.entityRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, entityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);

            worldState.translate(new Vector3f(entity.position()));
            bone.setWorldSpaceMatrix(worldState);
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (!isReRender && buffer instanceof BufferBuilder builder && !builder.building) {
            context.setVertexConsumer(bufferSource.getBuffer(renderType));
        }

        renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);

        poseStack.popPose();
    }

    /**
     * Applies rotation transformations to the renderer prior to render time to account for various entity states,
     * default scale of 1
     */
    protected void applyRotations(
        T animatable,
        PoseStack poseStack,
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
        PoseStack poseStack,
        float ageInTicks,
        float rotationYaw,
        float partialTick,
        float nativeScale
    ) {
        if (animatable.isFullyFrozen()) {
            rotationYaw += (float) (Math.cos(animatable.tickCount * 3.25d) * Math.PI * 0.4d);
        }

        if (animatable.getPose() != Pose.SLEEPING) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180f - rotationYaw));
        }

        if (animatable instanceof LivingEntity livingEntity) {
            var config = entityRendererPipeline.getRenderer().config();
            var deathMaxRotation = config.getDeathMaxRotation(animatable);

            if (livingEntity.deathTime > 0) {
                float deathRotation = (livingEntity.deathTime + partialTick - 1f) / 20f * 1.6f;

                poseStack.mulPose(
                    Vector3f.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1) * deathMaxRotation)
                );
            } else if (livingEntity.isAutoSpinAttack()) {
                poseStack.mulPose(Vector3f.XP.rotationDegrees(-90f - livingEntity.getXRot()));
                poseStack.mulPose(Vector3f.YP.rotationDegrees((livingEntity.tickCount + partialTick) * -75f));
            } else if (animatable.getPose() == Pose.SLEEPING) {
                Direction bedOrientation = livingEntity.getBedOrientation();

                poseStack.mulPose(
                    Vector3f.YP.rotationDegrees(
                        bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw
                    )
                );
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(deathMaxRotation));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(270f));
            } else if (LivingEntityRenderer.isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (animatable.getBbHeight() + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
            }
        }
    }
}
