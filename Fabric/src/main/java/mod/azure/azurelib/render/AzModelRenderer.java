package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * AzModelRenderer provides a generic and extensible base class for rendering models by processing hierarchical bone
 * structures recursively. It leverages a rendering pipeline and a layer renderer to facilitate advanced rendering
 * tasks, including layer application and animated texture processing.
 *
 * @param <T> the type of animatable object this renderer supports
 */
public class AzModelRenderer<T> {

    private final Matrix4f poseStateCache = new Matrix4f();

    private final Vector3f normalCache = new Vector3f();

    private final AzRendererPipeline<T> rendererPipeline;

    protected final AzLayerRenderer<T> layerRenderer;

    public AzModelRenderer(AzRendererPipeline<T> rendererPipeline, AzLayerRenderer<T> layerRenderer) {
        this.layerRenderer = layerRenderer;
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * The actual render method that sub-type renderers should override to handle their specific rendering tasks.<br>
     */
    protected void render(AzRendererPipelineContext<T> context, boolean isReRender) {
        T animatable = context.animatable();
        AzBakedModel model = context.bakedModel();

        rendererPipeline.updateAnimatedTextureFrame(animatable);

        for (AzBone bone : model.getTopLevelBones()) {
            renderRecursively(context, bone, isReRender);
        }
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    protected void renderRecursively(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        PoseStack poseStack = context.poseStack();

        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);
        renderCubesOfBone(context, bone);

        if (!isReRender) {
            layerRenderer.applyRenderLayersForBone(context, bone);
        }

        renderChildBones(context, bone, isReRender);
        poseStack.popPose();
    }

    /**
     * Renders the {@link GeoCube GeoCubes} associated with a given {@link AzBone}
     */
    protected void renderCubesOfBone(AzRendererPipelineContext<T> context, AzBone bone) {
        if (bone.isHidden()) {
            return;
        }

        PoseStack poseStack = context.poseStack();

        for (GeoCube cube : bone.getCubes()) {
            poseStack.pushPose();

            renderCube(context, cube);

            poseStack.popPose();
        }
    }

    /**
     * Render the child bones of a given {@link AzBone}.<br>
     * Note that this does not render the bone itself. That should be done through
     * {@link AzModelRenderer#renderCubesOfBone} separately
     */
    protected void renderChildBones(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        if (bone.isHidingChildren())
            return;

        for (AzBone childBone : bone.getChildBones()) {
            renderRecursively(context, childBone, isReRender);
        }
    }

    /**
     * Renders an individual {@link GeoCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<T> context, GeoCube cube) {
        PoseStack poseStack = context.poseStack();

        RenderUtils.translateToPivotPoint(poseStack, cube);
        RenderUtils.rotateMatrixAroundCube(poseStack, cube);
        RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

        Matrix3f normalisedPoseState = poseStack.last().normal();
        Matrix4f poseState = poseStack.last().pose();

        for (GeoQuad quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            Vector3f normal = quad.getNormal().copy();

            normal.transform(normalisedPoseState);

            RenderUtils.fixInvertedFlatCube(cube, normal);
            createVerticesOfQuad(context, quad, poseState, normal);
        }
    }

    private final Vector4f poseStateTransformCache = new Vector4f();

    /**
     * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering
     */
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<T> context,
        GeoQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        VertexConsumer buffer = context.vertexConsumer();
        int packedOverlay = context.packedOverlay();
        int packedLight = context.packedLight();

        for (GeoVertex vertex : quad.getVertices()) {
            Vector3f position = vertex.position();
            Vector4f vector4f = new Vector4f(position.x(), position.y(), position.z(), 1);

            vector4f.transform(poseState);

            buffer.vertex(
                vector4f.x(),
                vector4f.y(),
                vector4f.z(),
                context.red(),
                context.green(),
                context.blue(),
                context.alpha(),
                vertex.texU(),
                vertex.texV(),
                packedOverlay,
                packedLight,
                normal.x(),
                normal.y(),
                normal.z()
            );
        }
    }

    /**
     * Provides an override logic for rendering a specific bone. This method allows
     * custom handling of how an individual bone should be rendered.
     *
     * @param poseStack     The pose stack used for managing transformations during rendering.
     * @param bone          The bone targeted for rendering or customization.
     * @param bufferSource  The source of buffers used in the rendering process.
     * @param buffer        The vertex consumer to which the rendering data is submitted.
     * @param partialTick   The partial tick for interpolating transformations and animations.
     * @param packedLight   The packed lightmap coordinates for lighting calculations.
     * @param packedOverlay The packed overlay coordinates for additional rendering effects.
     * @param red           The red color multiplier applied to the bone's rendering.
     * @param green         The green color multiplier applied to the bone's rendering.
     * @param blue          The blue color multiplier applied to the bone's rendering.
     * @param alpha         The alpha/transparency value applied to the bone's rendering.
     * @return A boolean indicating whether this override has applied custom rendering logic.
     *         If true, the custom behavior has been applied; otherwise, the default rendering will proceed.
     */
    public boolean boneRenderOverride(
        PoseStack poseStack,
        AzBone bone,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay,
        float red,
        float green,
        float blue,
        float alpha
    ) {
        return false;
    }

    /**
     * Determines if a specific bone should override the default render type. This method can be used to apply custom
     * rendering behavior to individual bones by specifying a different {@link RenderType}.
     *
     * @param bone         The bone that is being considered for a render type override.
     * @param animatable   The animation-related object associated with this renderer, typically an entity or
     *                     geo-animatable instance.
     * @param texturePath  The resource location of the texture associated with the bone.
     * @param bufferSource The buffer source used for rendering operations.
     * @param partialTick  The partial tick progress for interpolating
     */
    @Nullable
    public RenderType getRenderTypeOverrideForBone(
        AzBone bone,
        T animatable,
        ResourceLocation texturePath,
        MultiBufferSource bufferSource,
        float partialTick
    ) {
        return null;
    }

    /**
     * Retrieves a texture override for a specific bone during rendering, if applicable. This method can be used to
     * specify a custom texture for individual bones.
     *
     * @param bone        The bone for which the texture override is being requested.
     * @param animatable  The animation-related object associated with the renderer, typically an entity or
     *                    geo-animatable instance.
     * @param partialTick The partial tick progress for interpolating animations.
     * @return A {@link ResourceLocation} pointing to the overridden texture for the specified bone, or null if no
     *         override is applied.
     */
    @Nullable
    public ResourceLocation getTextureOverrideForBone(AzBone bone, T animatable, float partialTick) {
        return null;
    }
}
