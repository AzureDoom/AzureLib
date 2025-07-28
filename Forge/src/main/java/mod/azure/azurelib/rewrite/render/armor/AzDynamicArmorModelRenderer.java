package mod.azure.azurelib.rewrite.render.armor;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;

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
        MatrixStack poseStack = context.poseStack();
        // TODO: This is dangerous.
        AzArmorRendererPipelineContext ctx = armorRendererPipeline.context();
        RenderType renderType = context.renderType();
        IVertexBuilder buffer = context.vertexConsumer();
        IRenderTypeBuffer bufferSource = context.multiBufferSource();

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.getLast().getMatrix());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(
                poseState,
                armorRendererPipeline.entityRenderTranslations
            );
            Matrix4f worldState = localMatrix.copy();

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, armorRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(localMatrix);

            worldState.translate(new Vector3f(ctx.currentEntity().getPositionVec()));
            bone.setWorldSpaceMatrix(worldState);
        }

        AzArmorRendererConfig config = armorRendererPipeline.config();

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
            renderTypeOverride != null && renderType != null && !isReRender && buffer instanceof BufferBuilder
                && !((BufferBuilder) buffer).isDrawing()
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

        AzRendererConfig<ItemStack> config = armorRendererPipeline.config();
        Tuple<Integer, Integer> boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        Tuple<Integer, Integer> entityTextureSize = context.computeTextureSize(config.textureLocation(context.animatable()));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        for (GeoVertex vertex : quad.getVertices()) {
            Vector3f position = vertex.position();
            Vector4f vector4f = new Vector4f(position.getX(), position.getY(), position.getZ(), 1);

            vector4f.transform(poseState);
            float texU = (vertex.texU() * entityTextureSize.getA()) / boneTextureSize.getA();
            float texV = (vertex.texV() * entityTextureSize.getB()) / boneTextureSize.getB();

            context.vertexConsumer().addVertex(
                vector4f.getX(),
                vector4f.getY(),
                vector4f.getZ(),
                context.red(),
                context.green(),
                context.blue(),
                context.alpha(),
                texU,
                texV,
                context.packedOverlay(),
                context.packedLight(),
                normal.getX(),
                normal.getY(),
                normal.getZ()
            );
        }
    }
}
