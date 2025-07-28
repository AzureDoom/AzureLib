package mod.azure.azurelib.rewrite.render.entity;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * AzDynamicEntityModelRenderer is an abstract class designed for rendering dynamic entity models with support for
 * complex transformations, render layers, and texture overrides. It extends the functionality provided by the base
 * AzEntityModelRenderer to allow for advanced rendering features such as bone hierarchy processing, vertex
 * manipulation, and configurable rendering pipelines.
 *
 * @param <T> The type of entity to be rendered, which must extend the Entity class.
 */
public abstract class AzDynamicEntityModelRenderer<T extends Entity> extends AzEntityModelRenderer<T> {

    public AzDynamicEntityModelRenderer(
        AzEntityRendererPipeline<T> entityRendererPipeline,
        AzLayerRenderer<T> layerRenderer
    ) {
        super(entityRendererPipeline, layerRenderer);
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
                entityRendererPipeline.entityRenderTranslations
            );

            bone.setModelSpaceMatrix(
                RenderUtils.invertAndMultiplyMatrices(poseState, entityRendererPipeline.modelRenderTranslations)
            );
            bone.setLocalSpaceMatrix(
                RenderUtils.translateMatrix(
                    localMatrix,
                    entityRendererPipeline.getRenderer().getRenderOffset(entity, 1).toVector3f()
                )
            );
            bone.setWorldSpaceMatrix(
                RenderUtils.translateMatrix(new Matrix4f(localMatrix), entity.position().toVector3f())
            );
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);

        var config = entityRendererPipeline.config();

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

        var config = entityRendererPipeline.config();
        IntIntPair boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        IntIntPair entityTextureSize = context.computeTextureSize(config.textureLocation(context.animatable()));

        if (boneTextureSize == null || entityTextureSize == null) {
            super.createVerticesOfQuad(context, quad, poseState, normal);

            return;
        }

        for (GeoVertex vertex : quad.vertices()) {
            Vector4f vector4f = poseState.transform(
                new Vector4f(vertex.position().x(), vertex.position().y(), vertex.position().z(), 1.0f)
            );
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
