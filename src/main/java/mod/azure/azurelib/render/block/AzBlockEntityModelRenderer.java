package mod.azure.azurelib.render.block;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import mod.azure.azurelib.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.AzLayerRenderer;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.AzRendererPipelineContext;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.render.vertex.VertexConsumer;
import mod.azure.azurelib.util.client.RenderUtils;
import mod.azure.azurelib.util.math.Matrix4f;
import mod.azure.azurelib.util.math.Vector3f;

/**
 * The AzBlockEntityModelRenderer is a specialized model renderer class for rendering block entities in a 3D space. It
 * extends the AzModelRenderer class and provides functionality specific to handling and rendering block entities based
 * on their corresponding properties and transformations.
 *
 * @param <T> The type of TileEntity that this renderer is responsible for
 */
public class AzBlockEntityModelRenderer<T extends TileEntity> extends AzModelRenderer<Long, T> {

    protected final AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline;

    private final Matrix4f scratchPoseState = new Matrix4f();

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
        T entity = context.animatable();
        PoseStack poseStack = context.poseStack();

        if (!isReRender) {

            poseStack.translate(0.5, 0, 0.5);
            rotateBlock(getFacing(entity), poseStack);
            AzBlockAnimator<T> animator = blockEntityRendererPipeline.getRenderer().getAnimator();

            if (animator != null || context.applyAnimationOnReRender()) {
                handleAnimation(animator, entity, context.partialTick());
            }
        }

        blockEntityRendererPipeline.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        super.render(context, isReRender);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<Long, T> context, AzBone bone, boolean isReRender) {
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
                    blockEntityRendererPipeline.entityRenderTranslations
                );

                bone.setModelSpaceMatrix(
                    RenderUtils.invertAndMultiplyMatrices(
                        scratchPoseState,
                        blockEntityRendererPipeline.modelRenderTranslations
                    )
                );
                bone.setLocalSpaceMatrix(
                    RenderUtils.translateMatrix(localMatrix, new Vector3f())
                );
                bone.setWorldSpaceMatrix(
                    RenderUtils.translateMatrix(
                        localMatrix.copy(),
                        new Vector3f(
                            entity.getPos().getX(),
                            entity.getPos().getY(),
                            entity.getPos().getZ()
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
     * Attempt to extract a direction from the block so that the model can be oriented correctly
     */
    protected EnumFacing getFacing(T block) {
        if (!block.hasWorld())
            return EnumFacing.NORTH;

        IBlockState blockState = block.getWorld().getBlockState(block.getPos());

        if (blockState.getPropertyKeys().contains(BlockHorizontal.FACING))
            return blockState.getValue(BlockHorizontal.FACING);

        if (blockState.getPropertyKeys().contains(BlockDirectional.FACING))
            return blockState.getValue(BlockDirectional.FACING);

        return EnumFacing.NORTH;
    }

    /**
     * Rotate the {@link PoseStack} based on the determined {@link EnumFacing} the block is facing
     */
    protected void rotateBlock(EnumFacing facing, PoseStack poseStack) {
        switch (facing) {
            case SOUTH:
                poseStack.mulPose(Vector3f.YP.rotationDegrees(180));
                break;
            case WEST:
                poseStack.mulPose(Vector3f.YP.rotationDegrees(90));
                break;
            case NORTH:
                poseStack.mulPose(Vector3f.YP.rotationDegrees(0));
                break;
            case EAST:
                poseStack.mulPose(Vector3f.YP.rotationDegrees(270));
                break;
            case UP:
                poseStack.mulPose(Vector3f.XP.rotationDegrees(90));
                break;
            case DOWN:
                poseStack.mulPose(Vector3f.XN.rotationDegrees(90));
                break;
        }
    }
}
