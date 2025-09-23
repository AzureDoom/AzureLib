package mod.azure.azurelib.rewrite.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.cache.object.GeoCube;
import mod.azure.azurelib.common.internal.common.cache.object.GeoQuad;
import mod.azure.azurelib.common.internal.common.cache.object.GeoVertex;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.model.AzBone;

/**
 * AzModelRenderer provides a generic and extensible base class for rendering models by processing hierarchical bone
 * structures recursively. It leverages a rendering pipeline and a layer renderer to facilitate advanced rendering
 * tasks, including layer application and animated texture processing.
 *
 * @param <T> the type of animatable object this renderer supports
 */
public class AzModelRenderer<T> {

    private final Matrix4f poseStateCache = new Matrix4f();

    private final AzRendererPipeline<T> rendererPipeline;

    protected final AzLayerRenderer<T> layerRenderer;

    public AzModelRenderer(AzRendererPipeline<T> rendererPipeline, AzLayerRenderer<T> layerRenderer) {
        this.layerRenderer = layerRenderer;
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     */
    protected void render(AzRendererPipelineContext<T> context, boolean isReRender) {
        var animatable = context.animatable();
        var model = context.bakedModel();

        rendererPipeline.updateAnimatedTextureFrame(animatable);

        for (var bone : model.getTopLevelBones()) {
            renderRecursively(context, bone, isReRender);
        }

        var config = rendererPipeline.config();
        config.renderEntry(context);
    }

    /**
     * Renders the provided {@link AzBone} and its associated child bones
     */
    protected void renderRecursively(AzRendererPipelineContext<T> context, AzBone bone, boolean isReRender) {
        var poseStack = context.poseStack();

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

        var poseStack = context.poseStack();

        for (var cube : bone.getCubes()) {
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

        for (var childBone : bone.getChildBones()) {
            renderRecursively(context, childBone, isReRender);
        }
    }

    /**
     * Renders an individual {@link GeoCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<T> context, GeoCube cube) {
        var poseStack = context.poseStack();

        RenderUtils.translateToPivotPoint(poseStack, cube);
        RenderUtils.rotateMatrixAroundCube(poseStack, cube);
        RenderUtils.translateAwayFromPivotPoint(poseStack, cube);

        var normalisedPoseState = poseStack.last().normal();
        var poseState = poseStateCache.set(poseStack.last().pose());

        for (var quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            var normal = normalisedPoseState.transform(new Vector3f(quad.normal()));

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
        var buffer = context.vertexConsumer();
        var color = context.renderColor();
        var packedOverlay = context.packedOverlay();
        var packedLight = context.packedLight();

        for (var vertex : quad.vertices()) {
            var position = vertex.position();
            poseStateTransformCache.set(position.x(), position.y(), position.z(), 1.0f);
            var vector4f = poseState.transform(poseStateTransformCache);

            buffer.addVertex(
                vector4f.x(),
                vector4f.y(),
                vector4f.z(),
                color,
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
     * Override method for rendering a specific bone. This method can be customized to apply specific transformations,
     * modify the bone's render properties, or override its rendering behavior entirely.
     *
     * @param poseStack     The pose stack used for handling transformations (rotation, scaling, and translation).
     * @param bone          The bone that is being rendered.
     * @param bufferSource  The buffer source used for rendering.
     * @param buffer        The vertex consumer buffer used for writing vertex data during rendering.
     * @param partialTick   The partial tick progress for interpolating between frames.
     * @param packedLight   The packed light value for the rendered bone.
     * @param packedOverlay The packed overlay value for the rendered bone.
     * @param colour        The color modifier for the rendered output.
     * @return A boolean indicating whether the bone's rendering behavior has been overridden successfully.
     */
    public boolean boneRenderOverride(
        PoseStack poseStack,
        AzBone bone,
        MultiBufferSource bufferSource,
        VertexConsumer buffer,
        float partialTick,
        int packedLight,
        int packedOverlay,
        int colour
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

    public void handleAnimation(AzAnimator<T> animator, T animatable, float partialTick) {
        animator.animate(animatable, partialTick);
    }

    /**
     * Retrieves the appropriate {@link VertexConsumer} for rendering, or refreshes the render buffer if needed.
     * Depending on the rendering context and state of the current buffer, this method determines whether to reuse the
     * existing buffer or acquire a new one.
     *
     * @param isReRender Indicates whether this is a re-render operation. If true, the current buffer is reused.
     * @param context    The rendering context containing relevant information like the current buffer, buffer source,
     *                   and render type.
     * @return The {@link VertexConsumer} that should be used for rendering, potentially refreshed based on the buffer's
     *         state and the given render context.
     */
    public VertexConsumer getOrRefreshRenderBuffer(boolean isReRender, AzRendererPipelineContext<T> context) {
        var currentBuffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var renderType = context.renderType();

        if (isReRender) {
            return currentBuffer;
        }

        return switch (currentBuffer) {
            case BufferBuilder builder when isBufferInactive(builder) -> bufferSource.getBuffer(renderType);
            case OutlineBufferSource.EntityOutlineGenerator outline when needsBufferRefresh(outline.delegate()) ->
                new OutlineBufferSource.EntityOutlineGenerator(bufferSource.getBuffer(renderType), outline.color());
            case VertexMultiConsumer.Double pair when needsBufferRefresh(pair.first) || needsBufferRefresh(
                pair.second
            ) ->
                new VertexMultiConsumer.Double(
                    needsBufferRefresh(pair.first) ? bufferSource.getBuffer(renderType) : pair.first,
                    needsBufferRefresh(pair.second) ? bufferSource.getBuffer(renderType) : pair.second
                );
            default -> currentBuffer;
        };
    }

    /**
     * Determines whether the given {@link VertexConsumer} requires a buffer refresh. This involves checking the
     * specific type of the {@link VertexConsumer} and applying appropriate logic to evaluate its state.
     *
     * @param buffer The {@link VertexConsumer} instance to evaluate.
     * @return {@code true} if the buffer needs to be refreshed; {@code false} otherwise.
     */
    private boolean needsBufferRefresh(VertexConsumer buffer) {
        return switch (buffer) {
            case BufferBuilder builder -> isBufferInactive(builder);
            case OutlineBufferSource.EntityOutlineGenerator outline -> needsBufferRefresh(outline.delegate());
            case VertexMultiConsumer.Double pair ->
                needsBufferRefresh(pair.first) || needsBufferRefresh(pair.second);
            default -> false;
        };
    }

    /**
     * Determines if the given {@link BufferBuilder} is inactive. A buffer is considered inactive if it is not currently
     * in the process of building.
     *
     * @param builder The {@link BufferBuilder} instance to check.
     * @return {@code true} if the buffer is inactive (not building); {@code false} otherwise.
     */
    private boolean isBufferInactive(BufferBuilder builder) {
        return !builder.building;
    }
}
