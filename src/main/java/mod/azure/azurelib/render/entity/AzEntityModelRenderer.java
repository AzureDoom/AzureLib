package mod.azure.azurelib.render.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.UUID;

import mod.azure.azurelib.animation.impl.AzEntityAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Direction;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Mth;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * AzEntityModelRenderer is a class responsible for rendering animated 3D entity models in a pipeline-based rendering
 * setup. Extends the {@link AzModelRenderer} class and utilizes the {@link AzEntityRendererPipeline} to handle various
 * rendering tasks, such as applying model transformations and managing animated states in the rendering lifecycle. <br>
 *
 * @param <T> The type of entity that this renderer applies to, extends the {@link Entity} class.
 */
public class AzEntityModelRenderer<T extends Entity> extends AzModelRenderer<UUID, T> {

    protected final AzEntityRendererPipeline<T> entityRendererPipeline;

    private final Matrix4f scratchPoseState = new Matrix4f();

    private final Matrix4f scratchLocalMatrix = new Matrix4f();

    public AzEntityModelRenderer(
        AzEntityRendererPipeline<T> entityRendererPipeline,
        AzLayerRenderer<UUID, T> layerRenderer
    ) {
        super(entityRendererPipeline, layerRenderer);
        this.entityRendererPipeline = entityRendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link AzEntityRendererPipeline#preRender} has already been called by this stage, and
     * {@link AzEntityRendererPipeline#postRender} will be called directly after
     */
    @Override
    public void render(AzRendererPipelineContext<UUID, T> context, boolean isReRender) {
        T animatable = context.animatable();
        float partialTick = context.partialTick();
        PoseStack poseStack = context.poseStack();

        poseStack.pushPose();

        float lerpBodyRot = getLerpRot(animatable, partialTick);

        if (isSleeping(animatable)) {
            Direction bedDirection = getBedOrientation(animatable);

            if (bedDirection != null) {
                float eyePosOffset = ((LivingEntity) animatable).getEyeHeight() - 0.1F;

                poseStack.translate(
                    -bedDirection.getStepX() * eyePosOffset,
                    0,
                    -bedDirection.getStepZ() * eyePosOffset
                );
            }
        }

        float nativeScale = 1; // Beta has no baby mobs
        float ageInTicks = Mc.age(animatable) + partialTick;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!isReRender || context.applyAnimationOnReRender()) {
            AzEntityRenderer<T> renderer = entityRendererPipeline.getRenderer();
            AzEntityAnimator<T> animator = renderer.getAnimator();

            if (animator != null && renderer.shouldAnimateThisFrame()) {
                handleAnimation(animator, animatable, context.partialTick());
            }
        }

        entityRendererPipeline.modelRenderTranslations.load(poseStack.last().pose());

        if (context.vertexConsumer() != null) {
            super.render(context, isReRender);
        }

        poseStack.popPose();
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<UUID, T> context, AzBone bone, boolean isReRender) {
        VertexConsumer buffer = context.vertexConsumer();
        MultiBufferSource bufferSource = context.multiBufferSource();
        T entity = context.animatable();
        PoseStack poseStack = context.poseStack();

        int slot = saveBonePose(poseStack);

        try {
            RenderUtils.translateMatrixToBone(poseStack, bone);
            RenderUtils.translateToPivotPoint(poseStack, bone);
            RenderUtils.rotateMatrixAroundBone(poseStack, bone);
            RenderUtils.scaleMatrixForBone(poseStack, bone);

            if (bone.isTrackingMatrices()) {
                scratchPoseState.load(poseStack.last().pose());
                Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                    scratchPoseState,
                    entityRendererPipeline.entityRenderTranslations
                );
                bone.setModelSpaceMatrix(
                    RenderUtils.invertAndMultiplyMatrices(
                        scratchPoseState,
                        entityRendererPipeline.modelRenderTranslations
                    )
                );
                scratchLocalMatrix.load(localMatrix);
                RenderUtils.translateMatrixInPlace(
                    scratchLocalMatrix,
                    new Vector3f(entityRendererPipeline.getRenderer().getRenderOffset(entity, 1))
                );
                bone.setLocalSpaceMatrix(localMatrix);
                bone.setWorldSpaceMatrix(scratchLocalMatrix);
            }

            RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

            context.setVertexConsumer(getOrRefreshRenderBuffer(isReRender, context, bone));

            if (
                !boneRenderOverride(
                    poseStack,
                    bone,
                    bufferSource,
                    buffer,
                    context.partialTick(),
                    context.packedLight(),
                    context.packedOverlay(),
                    context.red(),
                    context.green(),
                    context.blue(),
                    context.alpha()
                )
            )
                super.renderCubesOfBone(context, bone);

            if (!isReRender) {
                layerRenderer.applyRenderLayersForBone(context, bone);
            }

            renderChildBones(context, bone, isReRender);
        } finally {
            restoreBonePose(poseStack, slot);
        }
    }

    /**
     * Calculates a linear interpolation (LERP) rotation value for a given entity, taking into account the entity's
     * current and previous rotations, its head movement, and whether it is mounted on another entity. Specifically,
     * this method interpolates between the previous and current rotation states, constraining rotational adjustments to
     * ensure realistic movement, especially when the entity is a passenger.
     *
     * @param animatable  The entity whose rotation is to be interpolated. Must extend {@link Entity}, and may include
     *                    subtypes such as {@link LivingEntity} to apply specific logic for living entities.
     * @param partialTick A float value representing the partial time progression within the current game tick. Used to
     *                    blend between previous and current states for smoother animations.
     * @return The interpolated LERP rotation value, which represents the adjusted body rotation of the entity after
     *         considering multiple elements such as head movements and passenger state.
     */
    private static <T extends Entity> float getLerpRot(T animatable, float partialTick) {
        boolean shouldSit = Mc.isRiding(animatable) && (Mc.vehicle(animatable) != null);

        float lerpBodyRot = animatable instanceof LivingEntity
            ? Mth.rotLerp(
                partialTick,
                Mc.prevBodyYaw((LivingEntity) animatable),
                Mc.bodyYaw((LivingEntity) animatable)
            )
            : Mc.yaw(animatable);
        float lerpHeadRot = animatable instanceof LivingEntity
            ? Mth.rotLerp(
                partialTick,
                Mc.prevHeadYaw((LivingEntity) animatable),
                Mc.headYaw((LivingEntity) animatable)
            )
            : Mc.yaw(animatable);

        if (shouldSit && Mc.vehicle(animatable) instanceof LivingEntity) {
            LivingEntity livingentity = (LivingEntity) Mc.vehicle(animatable);
            lerpBodyRot = Mth.rotLerp(partialTick, Mc.prevBodyYaw(livingentity), Mc.bodyYaw(livingentity));
            float netHeadYaw = lerpHeadRot - lerpBodyRot;
            float clampedHeadYaw = Mth.clamp(Mth.wrapDegrees(netHeadYaw), -85, 85);
            lerpBodyRot = lerpHeadRot - clampedHeadYaw;

            if (clampedHeadYaw * clampedHeadYaw > 2500f)
                lerpBodyRot += clampedHeadYaw * 0.2f;
        }
        return lerpBodyRot;
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
        if (!isSleeping(animatable)) {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180f - rotationYaw));
        }

        if (animatable instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) animatable;
            AzEntityRendererConfig<T> config = entityRendererPipeline.getRenderer().config();
            float deathMaxRotation = config.getDeathMaxRotation(animatable);

            if (Mc.deathTime(livingEntity) > 0) {
                float deathRotation = (Mc.deathTime(livingEntity) + partialTick - 1f) / 20f * 1.6f;

                poseStack.mulPose(
                    Vector3f.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1) * deathMaxRotation)
                );
            } else if (isSleeping(animatable)) {
                Direction bedOrientation = getBedOrientation(animatable);

                poseStack.mulPose(
                    Vector3f.YP.rotationDegrees(
                        bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw
                    )
                );
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(deathMaxRotation));
                poseStack.mulPose(Vector3f.YP.rotationDegrees(270f));
            } else if (isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (Mc.height(animatable) + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
            }
        }
    }

    private static boolean isSleeping(Entity entity) {
        return entity instanceof LivingEntity && Mc.isSleeping((LivingEntity) entity);
    }

    /**
     * The direction of the bed a sleeping player lies in. Beta does not expose it through a stable mapped accessor, so
     * this returns {@code null} and the body keeps its yaw; override if you need exact bed alignment.
     */
    protected Direction getBedOrientation(Entity entity) {
        return null;
    }

    /** Beta predates the "Dinnerbone" easter egg, so nothing is ever rendered upside down. */
    private static boolean isEntityUpsideDown(LivingEntity entity) {
        return false;
    }
}
