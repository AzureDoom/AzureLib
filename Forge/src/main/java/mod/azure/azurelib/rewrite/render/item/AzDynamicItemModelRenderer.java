package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

/**
 * AzDynamicItemModelRenderer is an abstract extension of AzItemModelRenderer designed to handle dynamic
 * transformations, layer configurations, and rendering logic for item models in intricate circumstances. It focuses on
 * advanced rendering operations, including recursive rendering of bone hierarchies and vertex-level manipulation.
 */
public abstract class AzDynamicItemModelRenderer extends AzItemModelRenderer {

    public AzDynamicItemModelRenderer(
        AzItemRendererPipeline itemRendererPipeline,
        AzLayerRenderer<ItemStack> layerRenderer
    ) {
        super(itemRendererPipeline, layerRenderer);
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
    public void renderRecursively(AzRendererPipelineContext<ItemStack> context, AzBone bone, boolean isReRender) {
        RenderType renderType = context.renderType();
        IVertexBuilder buffer = context.vertexConsumer();
        IRenderTypeBuffer bufferSource = context.multiBufferSource();
        MatrixStack poseStack = context.poseStack();

        if (bone.isTrackingMatrices()) {
            ItemStack animatable = context.animatable();
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                itemRendererPipeline.itemRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, itemRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, itemRendererPipeline.modelRenderTranslations)
            );
            worldState.translate(new Vector3f(getRenderOffset(animatable, 1)));
            bone.setWorldSpaceMatrix(worldState);
        }

        AzRendererConfig<ItemStack> config = itemRendererPipeline.config();

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
            renderTypeOverride = context.getDefaultRenderType(context.animatable(), texture, context.multiBufferSource(), context.partialTick());
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

        super.renderRecursively(context, bone, isReRender);
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
        AzRendererPipelineContext<ItemStack> context,
        GeoQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        if (context.getTextureOverride() == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        AzRendererConfig<ItemStack> config = itemRendererPipeline.config();
        Tuple<Integer, Integer> boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        Tuple<Integer, Integer> entityTextureSize = context.computeTextureSize(config.textureLocation(context.animatable()));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        for (GeoVertex vertex : quad.getVertices()) {
            Vector3f position = vertex.position();
            Vector4f vector4f = new Vector4f(position.x(), position.y(), position.z(), 1);

            vector4f.transform(poseState);
            float texU = (vertex.texU() * entityTextureSize.getA()) / boneTextureSize.getA();
            float texV = (vertex.texV() * entityTextureSize.getB()) / boneTextureSize.getB();

            context.vertexConsumer().vertex(
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
