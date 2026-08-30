package mod.azure.azurelib.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.AzVertexCapture;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * The AzBlockEntityModelRenderer is a specialized model renderer class for rendering block entities in a 3D space. It
 * extends the AzModelRenderer class and provides functionality specific to handling and rendering block entities based
 * on their corresponding properties and transformations.
 *
 * @param <T> The type of BlockEntity that this renderer is responsible for
 */
public class AzBlockEntityModelRenderer<T extends BlockEntity> extends AzModelRenderer<Long, T> {

    protected final AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline;

    private boolean capturing = false;

    private AzVertexCapture captureConsumer = null;

    private RenderType captureRenderType = null;

    private boolean detectedMultipleRenderTypes = false;

    private long capturedBoneHash = 0L;

    public AzBlockEntityModelRenderer(
        AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline,
        AzLayerRenderer<Long, T> layerRenderer
    ) {
        super(blockEntityRendererPipeline, layerRenderer);
        this.blockEntityRendererPipeline = blockEntityRendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     * {@link AzBlockEntityRendererPipeline#preRender} has already been called by this stage, and
     * {@link AzBlockEntityRendererPipeline#postRender} will be called directly after
     */
    @Override
    public void render(AzRendererPipelineContext<Long, T> context, boolean isReRender) {
        var entity = context.animatable();
        var poseStack = context.poseStack();

        // See AzEntityModelRenderer#render for the rationale; this is the block-entity equivalent.
        float entityCamX = 0, entityCamY = 0, entityCamZ = 0;
        int passCamXBits = 0, passCamYBits = 0, passCamZBits = 0;
        if (!isReRender) {
            var m = poseStack.last().pose();
            entityCamX = m.m30();
            entityCamY = m.m31();
            entityCamZ = m.m32();
            var bpos = entity.getBlockPos();
            passCamXBits = Float.floatToRawIntBits(bpos.getX() - entityCamX);
            passCamYBits = Float.floatToRawIntBits(bpos.getY() - entityCamY);
            passCamZBits = Float.floatToRawIntBits(bpos.getZ() - entityCamZ);
        }

        if (!isReRender) {
            poseStack.translate(0.5, 0, 0.5);
            rotateBlock(getFacing(entity), poseStack);

            var animator = blockEntityRendererPipeline.getRenderer().getAnimator();
            if (animator != null || context.applyAnimationOnReRender()) {
                handleAnimation(animator, entity, context.partialTick());
            }
        }

        if (!isReRender) {
            var level = entity.getLevel();
            if (level != null) {
                AzBlockEntityGeometryCache.maybeReset(level.getGameTime(), context.partialTick());

                var model = context.bakedModel();
                var blockState = entity.getBlockState();
                var rt = context.renderType();

                if (rt != null && !anyBoneTracksMatrices(model.getTopLevelBones())) {
                    long boneHash = hashBones(model.getTopLevelBones());
                    capturedBoneHash = boneHash;

                    var cacheKey = new AzBlockEntityGeometryCache.CacheKey(
                        model.getModelUUID(),
                        blockState,
                        boneHash,
                        rt,
                        passCamXBits,
                        passCamYBits,
                        passCamZBits
                    );

                    var snapshot = AzBlockEntityGeometryCache.get(cacheKey);

                    if (snapshot != null) {
                        if (!AzBlockEntityGeometryCache.isUncacheable(snapshot)) {
                            snapshot.replayTo(
                                context.multiBufferSource(),
                                entityCamX,
                                entityCamY,
                                entityCamZ,
                                context.packedLight(),
                                context.packedOverlay(),
                                context.renderColor()
                            );
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

        blockEntityRendererPipeline.modelRenderTranslations.set(poseStack.last().pose());

        super.render(context, isReRender);

        if (capturing) {
            capturing = false;

            var model = context.bakedModel();
            var blockState = entity.getBlockState();
            var rt = context.renderType();
            var cacheKey = new AzBlockEntityGeometryCache.CacheKey(
                model.getModelUUID(),
                blockState,
                capturedBoneHash,
                rt,
                passCamXBits,
                passCamYBits,
                passCamZBits
            );

            if (detectedMultipleRenderTypes) {
                AzBlockEntityGeometryCache.put(cacheKey, AzBlockEntityGeometryCache.uncacheable());
            } else {
                AzBlockEntityGeometryCache.put(
                    cacheKey,
                    new AzBlockEntityGeometryCache.VertexSnapshot(
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
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<Long, T> context, AzBone bone, boolean isReRender) {
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var entity = context.animatable();
        var poseStack = context.poseStack();

        poseStack.pushPose();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                blockEntityRendererPipeline.entityRenderTranslations
            );

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, blockEntityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.translateMatrix(localMatrix, Vec3.ZERO.toVector3f())
            );
            bone.setWorldSpaceMatrix(
                RenderUtils.translateMatrix(
                    new Matrix4f(localMatrix),
                    new Vector3f(
                        entity.getBlockPos().getX(),
                        entity.getBlockPos().getY(),
                        entity.getBlockPos().getZ()
                    )
                )
            );
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
                context.renderColor()
            )
        )
            super.renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);

        poseStack.popPose();
    }

    /**
     * While a geometry-cache capture pass is in progress, swaps in {@link #captureConsumer} in place of the real buffer
     * so every vertex the model renderer emits is also recorded (see {@link AzBlockEntityGeometryCache}).
     */
    @Override
    public VertexConsumer getOrRefreshRenderBuffer(
        boolean isReRender,
        AzRendererPipelineContext<Long, T> context,
        AzBone bone
    ) {
        var realConsumer = super.getOrRefreshRenderBuffer(isReRender, context, bone);

        if (!capturing) {
            return realConsumer;
        }

        if (realConsumer == captureConsumer) {
            realConsumer = context.multiBufferSource().getBuffer(captureRenderType);
        }

        var config = blockEntityRendererPipeline.config();
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
     * Attempt to extract a direction from the block so that the model can be oriented correctly
     */
    protected Direction getFacing(T block) {
        BlockState blockState = block.getBlockState();

        if (blockState.hasProperty(HorizontalDirectionalBlock.FACING))
            return blockState.getValue(HorizontalDirectionalBlock.FACING);

        if (blockState.hasProperty(DirectionalBlock.FACING))
            return blockState.getValue(DirectionalBlock.FACING);

        return Direction.NORTH;
    }

    /**
     * Rotate the {@link PoseStack} based on the determined {@link Direction} the block is facing
     */
    protected void rotateBlock(Direction facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(0));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            case DOWN -> poseStack.mulPose(Axis.XN.rotationDegrees(90));
        }
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
}
