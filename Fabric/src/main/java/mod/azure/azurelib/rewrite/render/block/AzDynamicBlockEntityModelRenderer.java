package mod.azure.azurelib.rewrite.render.block;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.entity.BlockEntity;

import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * AzDynamicBlockEntityModelRenderer provides a specialized and extendable block entity renderer for dynamic, animated
 * models. It integrates with a rendering pipeline and handles advanced rendering, including bone transformations,
 * texture overrides, and layer-specific rendering. This class extends the functionality of AzBlockEntityModelRenderer
 * by adding more detailed control over how block entities are rendered in a dynamic and recursive manner.
 *
 * @param <T> The type of BlockEntity this renderer is designed to handle.
 */
public abstract class AzDynamicBlockEntityModelRenderer<T extends BlockEntity> extends AzBlockEntityModelRenderer<T> {

    public AzDynamicBlockEntityModelRenderer(
        AzBlockEntityRendererPipeline<T> blockEntityRendererPipeline,
        AzLayerRenderer<T> layerRenderer
    ) {
        super(blockEntityRendererPipeline, layerRenderer);
    }

    /**
     * Renders a bone and its child bones recursively with various transformations, texture settings, and layer
     * rendering configurations. The method ensures proper matrix transformations for the bone's position, rotation,
     * scale, and tracking, while handling texture and render type overrides as needed.
     *
     * @param context    The rendering context that holds the necessary pipeline data, like vertex consumers, render
     *                   types, animatable entities, and other contextual information required for rendering.
     * @param bone       The bone to be rendered. This includes its matrices, transformations, and other related
     *                   configurations.
     * @param isReRender A flag to indicate if this rendering process is a re-render pass. The flag influences
     *                   operations like applying render layers or reusing buffer configurations.
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

        var config = blockEntityRendererPipeline.config();

        context.setTextureOverride(getTextureOverrideForBone(bone, context.animatable(), context.partialTick()));

        ResourceLocation texture = context.getTextureOverride() == null
            ? config.textureLocation(context.animatable())
            : context.getTextureOverride();

        RenderType renderTypeOverride = getRenderTypeOverrideForBone(
            bone,
            context.animatable(),
            texture,
            bufferSource,
            context.partialTick()
        );

        if (texture != null && renderTypeOverride == null) {
            renderTypeOverride = context.getDefaultRenderType(
                context.animatable(),
                texture,
                context.multiBufferSource(),
                context.partialTick()
            );
            renderType = renderTypeOverride;
        }

        if (renderTypeOverride != null) {
            context.setVertexConsumer(bufferSource.getBuffer(renderTypeOverride));
            renderType = renderTypeOverride;
        }

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
     * Creates vertices for a quadrilateral (quad) during the rendering process. The method handles vertex
     * transformations, texture coordinate adjustments based on texture sizes, and passes the processed vertex data to
     * the vertex consumer for rendering. If no texture override is provided, it falls back to the default
     * implementation.
     *
     * @param context   The rendering pipeline context that includes necessary data like vertex consumers, texture
     *                  overrides, and associated configurations.
     * @param quad      The quadrilateral to be rendered, containing its vertices.
     * @param poseState A transformation matrix representing the current pose, used to transform vertex positions.
     * @param normal    The normal vector of the quad, used for light calculations and rendering purposes.
     */
    @Override
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<T> context,
        GeoQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        if (context.getTextureOverride() == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        var config = blockEntityRendererPipeline.config();
        Tuple<Integer, Integer> boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        Tuple<Integer, Integer> entityTextureSize = context.computeTextureSize(
            config.textureLocation(context.animatable())
        );

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            var position = vertex.position();
            Vector4f vector4f = new Vector4f(position.x(), position.y(), position.z(), 1);

            vector4f.transform(poseState);
            float texU = (vertex.texU() * entityTextureSize.getA()) / boneTextureSize.getA();
            float texV = (vertex.texV() * entityTextureSize.getB()) / boneTextureSize.getB();

            context.vertexConsumer()
                .vertex(
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    context.red(),
                    context.green(),
                    context.blue(),
                    context.alpha(),
                    texU,
                    texV,
                    context.packedOverlay(),
                    context.packedLight(),
                    normal.x(),
                    normal.y(),
                    normal.z()
                );
        }
    }
}
