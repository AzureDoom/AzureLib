package mod.azure.azurelib.rewrite.render.armor;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;

/**
 * The AzDynamicArmorModelRenderer class represents an abstract implementation of a dynamic armor model renderer. It is
 * a specialized extension of the AzArmorModelRenderer class, providing additional functionality for rendering armor
 * models dynamically with bone tracking, pose transformations, and texture override support.
 * <p>
 * This class handles complex animation and rendering tasks, including - Recursive rendering of bones with their
 * transformations and hierarchies. - Support for texture and render type overrides for specific bones. - Vertex
 * creation for quad meshes, including texture coordinate transformations.
 */
public abstract class AzDynamicArmorModelRenderer extends AzArmorModelRenderer {

    public AzDynamicArmorModelRenderer(
        AzArmorRendererPipeline armorRendererPipeline,
        AzLayerRenderer<ItemStack> layerRenderer
    ) {
        super(armorRendererPipeline, layerRenderer);
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
        var poseStack = context.poseStack();
        // TODO: This is dangerous.
        var ctx = armorRendererPipeline.context();
        var renderType = context.renderType();
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                armorRendererPipeline.entityRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, armorRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);

            worldState.translate(new Vector3f(ctx.currentEntity().position()));
            bone.setWorldSpaceMatrix(worldState);
        }

        var config = armorRendererPipeline.config();

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

        var config = armorRendererPipeline.config();
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
