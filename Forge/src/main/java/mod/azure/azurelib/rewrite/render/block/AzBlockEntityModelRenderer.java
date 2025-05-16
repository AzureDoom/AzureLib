package mod.azure.azurelib.rewrite.render.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.rewrite.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzModelRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.DirectionalBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * The AzBlockEntityModelRenderer is a specialized model renderer class for rendering block entities in a 3D space. It
 * extends the AzModelRenderer class and provides functionality specific to handling and rendering block entities based
 * on their corresponding properties and transformations.
 *
 * @param <T> The type of BlockEntity that this renderer is responsible for
 */
public class AzBlockEntityModelRenderer<T extends TileEntity> extends AzModelRenderer<T> {

    private final AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline;

    public AzBlockEntityModelRenderer(
        AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline,
        AzLayerRenderer<T> layerRenderer
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
    public void render(AzRendererPipelineContext<T> context, boolean isReRender) {
        T entity = context.animatable();
        MatrixStack poseStack = context.poseStack();

        if (!isReRender) {

            poseStack.translate(0.5, 0, 0.5);
            rotateBlock(getFacing(entity), poseStack);
            AzBlockAnimator<T> animator = blockEntityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                animator.animate(entity, context.partialTick());
            }
        }

        blockEntityRendererPipeline.modelRenderTranslations = new Matrix4f(poseStack.getLast().getMatrix());

        super.render(context, isReRender);
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
                blockEntityRendererPipeline.entityRenderTranslations
            );
            BlockPos pos = entity.getPos();
            Matrix4f worldState = new Matrix4f(localMatrix);

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, blockEntityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);
            worldState.translate(new Vector3f(pos.getX(), pos.getY(), pos.getZ()));
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
     * Attempt to extract a direction from the block so that the model can be oriented correctly
     */
    protected Direction getFacing(T block) {
        BlockState blockState = block.getBlockState();

        if (blockState.has(HorizontalBlock.HORIZONTAL_FACING))
            return blockState.get(HorizontalBlock.HORIZONTAL_FACING);

        if (blockState.has(DirectionalBlock.FACING))
            return blockState.get(DirectionalBlock.FACING);

        return Direction.NORTH;
    }

    /**
     * Rotate the {@link MatrixStack} based on the determined {@link Direction} the block is facing
     */
    protected void rotateBlock(Direction facing, MatrixStack poseStack) {
        switch (facing) {
            case SOUTH:
                poseStack.rotate(Vector3f.YP.rotationDegrees(180));
                break;
            case WEST:
                poseStack.rotate(Vector3f.YP.rotationDegrees(90));
                break;
            case NORTH:
                poseStack.rotate(Vector3f.YP.rotationDegrees(0));
                break;
            case EAST:
                poseStack.rotate(Vector3f.YP.rotationDegrees(270));
                break;
            case UP:
                poseStack.rotate(Vector3f.XP.rotationDegrees(90));
                break;
            case DOWN:
                poseStack.rotate(Vector3f.XN.rotationDegrees(90));
                break;
        }
    }
}
