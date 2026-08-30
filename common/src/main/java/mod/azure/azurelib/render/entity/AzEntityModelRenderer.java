package mod.azure.azurelib.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import org.joml.Matrix4f;

import java.util.List;
import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.AzVertexCapture;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * AzEntityModelRenderer is a class responsible for rendering animated 3D entity models in a pipeline-based rendering
 * setup. Extends the {@link AzModelRenderer} class and utilizes the {@link AzEntityRendererPipeline} to handle various
 * rendering tasks, such as applying model transformations and managing animated states in the rendering lifecycle. <br>
 * <p>
 * Since the whole pipeline now runs once per entity per frame during {@code extractRenderState} (see
 * {@link AzEntityRenderer}), this also opportunistically caches baked, entity-local geometry keyed by the pose/bone
 * state that produced it (see {@link AzEntityGeometryCache}), so that a frame containing many entities with an
 * identical animation state (e.g. a swarm of idle mobs) only has to walk the bone tree once.
 *
 * @param <T> The type of entity that this renderer applies to, extends the {@link Entity} class.
 */
public class AzEntityModelRenderer<T extends Entity> extends AzModelRenderer<UUID, T> {

    protected final AzEntityRendererPipeline<T> entityRendererPipeline;

    private final Matrix4f scratchPoseState = new Matrix4f();

    private final Matrix4f scratchLocalMatrix = new Matrix4f();

    private boolean capturing = false;

    private AzVertexCapture captureConsumer = null;

    private RenderType captureRenderType = null;

    private boolean detectedMultipleRenderTypes = false;

    private long capturedBoneHash = 0L;

    private long capturedPoseHash = 0L;

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
        var animatable = context.animatable();
        var partialTick = context.partialTick();
        var poseStack = context.poseStack();

        // The pipeline runs against an identity pose during extraction (see AzEntityRenderer#extractRenderState), so
        // this is the entity's position relative to the camera at the moment of capture, not its screen position.
        // It's what lets a cached snapshot be replayed correctly for a different entity/frame with a different
        // camera-relative offset: the delta between the two is added back on replay.
        float entityCamX = 0, entityCamY = 0, entityCamZ = 0;
        int passCamXBits = 0, passCamYBits = 0, passCamZBits = 0;
        if (!isReRender) {
            var m = poseStack.last().pose();
            entityCamX = m.m30();
            entityCamY = m.m31();
            entityCamZ = m.m32();

            float entityX = (float) Mth.lerp(partialTick, animatable.xo, animatable.getX());
            float entityY = (float) Mth.lerp(partialTick, animatable.yo, animatable.getY());
            float entityZ = (float) Mth.lerp(partialTick, animatable.zo, animatable.getZ());
            passCamXBits = Float.floatToRawIntBits(entityX - entityCamX);
            passCamYBits = Float.floatToRawIntBits(entityY - entityCamY);
            passCamZBits = Float.floatToRawIntBits(entityZ - entityCamZ);
        }

        poseStack.pushPose();
        float lerpBodyRot = getLerpRot(animatable, partialTick);

        if (animatable.getPose() == Pose.SLEEPING && animatable instanceof LivingEntity livingEntity) {
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

        float nativeScale = animatable instanceof LivingEntity livingEntity ? livingEntity.getScale() : 1;
        float ageInTicks = animatable.tickCount + partialTick;

        poseStack.scale(nativeScale, nativeScale, nativeScale);
        applyRotations(animatable, poseStack, ageInTicks, lerpBodyRot, partialTick, nativeScale);

        if (!isReRender || context.applyAnimationOnReRender()) {
            var animator = entityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                handleAnimation(animator, animatable, context.partialTick());
            }
        }

        entityRendererPipeline.modelRenderTranslations.set(poseStack.last().pose());

        if (!isReRender) {
            var level = animatable.level();
            if (level != null) {
                AzEntityGeometryCache.maybeReset(level.getGameTime(), partialTick);

                var model = context.bakedModel();
                var rt = context.renderType();

                if (rt != null && !anyBoneTracksMatrices(model.getTopLevelBones())) {
                    long boneHash = hashBones(model.getTopLevelBones());
                    long poseHash = hashPose(poseStack.last().pose(), entityCamX, entityCamY, entityCamZ);
                    capturedBoneHash = boneHash;
                    capturedPoseHash = poseHash;

                    var cacheKey = new AzEntityGeometryCache.CacheKey(
                        model.getModelUUID(),
                        animatable.getType(),
                        boneHash,
                        poseHash,
                        rt,
                        passCamXBits,
                        passCamYBits,
                        passCamZBits
                    );

                    var snapshot = AzEntityGeometryCache.get(cacheKey);

                    if (snapshot != null) {
                        if (!AzEntityGeometryCache.isUncacheable(snapshot)) {
                            snapshot.replayTo(
                                context.multiBufferSource(),
                                entityCamX,
                                entityCamY,
                                entityCamZ,
                                context.packedLight(),
                                context.packedOverlay(),
                                context.renderColor()
                            );
                            poseStack.popPose();
                            return;
                        }
                    } else {
                        capturing = true;
                        detectedMultipleRenderTypes = false;
                        captureRenderType = rt;

                        var realBuffer = context.multiBufferSource().getBuffer(rt);
                        if (captureConsumer == null) {
                            captureConsumer = new AzVertexCapture(realBuffer);
                        } else {
                            captureConsumer.reset(realBuffer);
                        }
                    }
                }
            }
        }

        if (context.vertexConsumer() != null) {
            super.render(context, isReRender);
        }

        if (capturing) {
            capturing = false;

            var model = context.bakedModel();
            var rt = context.renderType();
            var cacheKey = new AzEntityGeometryCache.CacheKey(
                model.getModelUUID(),
                animatable.getType(),
                capturedBoneHash,
                capturedPoseHash,
                rt,
                passCamXBits,
                passCamYBits,
                passCamZBits
            );

            if (detectedMultipleRenderTypes) {
                AzEntityGeometryCache.put(cacheKey, AzEntityGeometryCache.uncacheable());
            } else {
                AzEntityGeometryCache.put(
                    cacheKey,
                    new AzEntityGeometryCache.VertexSnapshot(
                        captureConsumer.getCapturedData(),
                        captureConsumer.getVertexCount(),
                        captureRenderType,
                        entityCamX,
                        entityCamY,
                        entityCamZ
                    )
                );
            }
        }

        poseStack.popPose();
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<UUID, T> context, AzBone bone, boolean isReRender) {
        if (bone.isHidden()) {
            if (!bone.isHidingChildren()) {
                renderChildBones(context, bone, isReRender);
            }
            return;
        }

        var previousBuffer = context.vertexConsumer();
        var previousTextureOverride = context.getTextureOverride();

        var bufferSource = context.multiBufferSource();
        var entity = context.animatable();
        var poseStack = context.poseStack();

        poseStack.pushPose();

        try {
            RenderUtils.translateMatrixToBone(poseStack, bone);
            RenderUtils.translateToPivotPoint(poseStack, bone);
            RenderUtils.rotateMatrixAroundBone(poseStack, bone);
            RenderUtils.scaleMatrixForBone(poseStack, bone);

            if (bone.isTrackingMatrices()) {
                scratchPoseState.set(poseStack.last().pose());
                var localMatrix = RenderUtils.invertAndMultiplyMatrices(
                    scratchPoseState,
                    entityRendererPipeline.entityRenderTranslations
                );
                bone.setModelSpaceMatrix(
                    RenderUtils.invertAndMultiplyMatrices(
                        scratchPoseState,
                        entityRendererPipeline.modelRenderTranslations
                    )
                );
                scratchLocalMatrix.set(localMatrix);
                RenderUtils.translateMatrixInPlace(
                    scratchLocalMatrix,
                    entityRendererPipeline.getRenderer().currentRenderOffset().toVector3f()
                );
                bone.setLocalSpaceMatrix(localMatrix);
                bone.setWorldSpaceMatrix(scratchLocalMatrix);
            }

            RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

            var boneBuffer = getOrRefreshRenderBuffer(isReRender, context, bone);
            context.setVertexConsumer(boneBuffer);

            if (
                !boneRenderOverride(
                    poseStack,
                    bone,
                    bufferSource,
                    boneBuffer,
                    context.partialTick(),
                    context.packedLight(),
                    context.packedOverlay(),
                    context.renderColor()
                )
            )
                super.renderCubesOfBone(context, bone);

            if (!isReRender) {
                layerRenderer.applyRenderLayersForBone(context, bone);
            }

            renderChildBones(context, bone, isReRender);
        } finally {
            context.setVertexConsumer(previousBuffer);
            context.setTextureOverride(previousTextureOverride);
            poseStack.popPose();
        }
    }

    /**
     * While a geometry-cache capture pass is in progress, swaps in {@link #captureConsumer} in place of the real buffer
     * so every vertex the model renderer emits is also recorded (see {@link AzEntityGeometryCache}). Bones with a
     * per-bone render type or texture override opt out of the shared capture, since those depend on more than the
     * shared pose/bone state that keys the cache.
     */
    @Override
    public com.mojang.blaze3d.vertex.VertexConsumer getOrRefreshRenderBuffer(
        boolean isReRender,
        AzRendererPipelineContext<UUID, T> context,
        AzBone bone
    ) {
        var realConsumer = super.getOrRefreshRenderBuffer(isReRender, context, bone);

        if (!capturing) {
            return realConsumer;
        }

        if (realConsumer == captureConsumer) {
            realConsumer = context.multiBufferSource().getBuffer(captureRenderType);
        }

        var config = entityRendererPipeline.config();
        if (
            config.boneRenderTypeOverrideProvider(bone) != null
                || config.boneTextureOverrideProvider(bone) != null
        ) {
            detectedMultipleRenderTypes = true;
            return realConsumer;
        }

        if (captureConsumer.delegate() != realConsumer) {
            captureConsumer.updateDelegate(realConsumer);
        }
        return captureConsumer;
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
        boolean shouldSit = animatable.isPassenger() && (animatable.getVehicle() != null);

        float lerpBodyRot = animatable instanceof LivingEntity livingEntity
            ? Mth.rotLerp(
                partialTick,
                livingEntity.yBodyRotO,
                livingEntity.yBodyRot
            )
            : animatable.getYRot();
        float lerpHeadRot = animatable instanceof LivingEntity livingEntity
            ? Mth.rotLerp(
                partialTick,
                livingEntity.yHeadRotO,
                livingEntity.yHeadRot
            )
            : animatable.getYHeadRot();

        if (shouldSit && animatable.getVehicle() instanceof LivingEntity livingentity) {
            lerpBodyRot = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
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

        if (!animatable.hasPose(Pose.SLEEPING)) {
            poseStack.mulPose(Axis.YP.rotationDegrees(180f - rotationYaw));
        }

        if (animatable instanceof LivingEntity livingEntity) {
            var config = entityRendererPipeline.getRenderer().config();
            var deathMaxRotation = config.getDeathMaxRotation(animatable);

            if (livingEntity.deathTime > 0) {
                float deathRotation = (livingEntity.deathTime + partialTick - 1f) / 20f * 1.6f;

                poseStack.mulPose(
                    Axis.ZP.rotationDegrees(Math.min(Mth.sqrt(deathRotation), 1) * deathMaxRotation)
                );
            } else if (livingEntity.isAutoSpinAttack()) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90f - livingEntity.getXRot()));
                poseStack.mulPose(Axis.YP.rotationDegrees((livingEntity.tickCount + partialTick) * -75f));
            } else if (animatable.hasPose(Pose.SLEEPING)) {
                Direction bedOrientation = livingEntity.getBedOrientation();

                poseStack.mulPose(
                    Axis.YP.rotationDegrees(
                        bedOrientation != null ? RenderUtils.getDirectionAngle(bedOrientation) : rotationYaw
                    )
                );
                poseStack.mulPose(Axis.ZP.rotationDegrees(deathMaxRotation));
                poseStack.mulPose(Axis.YP.rotationDegrees(270f));
            } else if (isEntityUpsideDown(livingEntity)) {
                poseStack.translate(0, (animatable.getBbHeight() + 0.1f) / nativeScale, 0);
                poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            }
        }
    }

    private static boolean isEntityUpsideDown(LivingEntity livingEntity) {
        if (!livingEntity.hasCustomName()) {
            return false;
        }

        var name = livingEntity.getName().getString();

        return "Dinnerbone".equals(name) || "Grumm".equals(name);
    }

    private static long hashBones(List<AzBone> bones) {
        long h = 1L;
        for (var bone : bones) {
            h = hashBoneRecursive(h, bone);
        }
        return h;
    }

    private static long hashBoneRecursive(long h, AzBone bone) {
        h = h * 31 + Float.floatToRawIntBits(bone.getPosX());
        h = h * 31 + Float.floatToRawIntBits(bone.getPosY());
        h = h * 31 + Float.floatToRawIntBits(bone.getPosZ());
        h = h * 31 + Float.floatToRawIntBits(bone.getRotX());
        h = h * 31 + Float.floatToRawIntBits(bone.getRotY());
        h = h * 31 + Float.floatToRawIntBits(bone.getRotZ());
        h = h * 31 + Float.floatToRawIntBits(bone.getScaleX());
        h = h * 31 + Float.floatToRawIntBits(bone.getScaleY());
        h = h * 31 + Float.floatToRawIntBits(bone.getScaleZ());
        for (var child : bone.getChildBones()) {
            h = hashBoneRecursive(h, child);
        }
        return h;
    }

    private static boolean anyBoneTracksMatrices(List<AzBone> bones) {
        for (var bone : bones) {
            if (bone.isTrackingMatrices())
                return true;
            if (anyBoneTracksMatrices(bone.getChildBones()))
                return true;
        }
        return false;
    }

    private static long hashPose(Matrix4f matrix, float entityCamX, float entityCamY, float entityCamZ) {
        long h = 1L;
        h = h * 31 + Float.floatToRawIntBits(matrix.m00());
        h = h * 31 + Float.floatToRawIntBits(matrix.m01());
        h = h * 31 + Float.floatToRawIntBits(matrix.m02());
        h = h * 31 + Float.floatToRawIntBits(matrix.m10());
        h = h * 31 + Float.floatToRawIntBits(matrix.m11());
        h = h * 31 + Float.floatToRawIntBits(matrix.m12());
        h = h * 31 + Float.floatToRawIntBits(matrix.m20());
        h = h * 31 + Float.floatToRawIntBits(matrix.m21());
        h = h * 31 + Float.floatToRawIntBits(matrix.m22());
        h = h * 31 + Float.floatToRawIntBits(matrix.m30() - entityCamX);
        h = h * 31 + Float.floatToRawIntBits(matrix.m31() - entityCamY);
        h = h * 31 + Float.floatToRawIntBits(matrix.m32() - entityCamZ);
        return h;
    }
}
