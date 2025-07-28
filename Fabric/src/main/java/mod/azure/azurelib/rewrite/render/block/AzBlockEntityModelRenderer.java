package mod.azure.azurelib.rewrite.render.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mod.azure.azurelib.rewrite.animation.impl.AzBlockAnimator;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzModelRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * The AzBlockEntityModelRenderer is a specialized model renderer class for rendering block entities in a 3D space. It
 * extends the AzModelRenderer class and provides functionality specific to handling and rendering block entities based
 * on their corresponding properties and transformations.
 *
 * @param <T> The type of BlockEntity that this renderer is responsible for
 */
public class AzBlockEntityModelRenderer<T extends BlockEntity> extends AzModelRenderer<T> {

    protected final AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline;

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
        PoseStack poseStack = context.poseStack();

        if (!isReRender) {

            poseStack.translate(0.5, 0, 0.5);
            rotateBlock(getFacing(entity), poseStack);
            AzBlockAnimator<T> animator = blockEntityRendererPipeline.getRenderer().getAnimator();

            if (animator != null) {
                animator.animate(entity, context.partialTick());
            }
        }

        blockEntityRendererPipeline.modelRenderTranslations = new Matrix4f(poseStack.last().pose());

        super.render(context, isReRender);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    @Override
    public void renderRecursively(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        VertexConsumer buffer = context.vertexConsumer();
        MultiBufferSource bufferSource = context.multiBufferSource();
        T entity = context.animatable();
        PoseStack poseStack = context.poseStack();
        RenderType renderType = context.renderType();

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
            BlockPos pos = entity.getBlockPos();
            Matrix4f worldState = new Matrix4f(localMatrix);

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, blockEntityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);
            worldState.translate(new Vector3f(pos.getX(), pos.getY(), pos.getZ()));
            bone.setWorldSpaceMatrix(worldState);
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        if (!isReRender && buffer instanceof BufferBuilder && !((BufferBuilder) buffer).building) {
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
