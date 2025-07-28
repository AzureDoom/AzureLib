package mod.azure.azurelib.rewrite.render.item;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

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
        var renderType = context.renderType();
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var poseStack = context.poseStack();

        if (bone.isTrackingMatrices()) {
            var animatable = context.animatable();
            var poseState = new Matrix4f(poseStack.last().pose());
            var localMatrix = RenderUtils.invertAndMultiplyMatrices(
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

        var config = itemRendererPipeline.config();

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

        if (texture != null && renderTypeOverride == null)
            renderTypeOverride = renderType;

        if (renderTypeOverride != null)
            context.setVertexConsumer(bufferSource.getBuffer(renderTypeOverride));

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

        if (
            renderTypeOverride != null && renderType != null && !isReRender && buffer instanceof BufferBuilder builder
                && !builder.building
        ) {
            context.setVertexConsumer(bufferSource.getBuffer(renderTypeOverride));
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

        var config = itemRendererPipeline.config();
        IntIntPair boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        IntIntPair entityTextureSize = context.computeTextureSize(config.textureLocation(context.animatable()));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            var position = vertex.position();
            Vector4f vector4f = new Vector4f(position.x(), position.y(), position.z(), 1);

            vector4f.transform(poseState);
            float texU = (vertex.texU() * entityTextureSize.firstInt()) / boneTextureSize.firstInt();
            float texV = (vertex.texV() * entityTextureSize.secondInt()) / boneTextureSize.secondInt();

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
