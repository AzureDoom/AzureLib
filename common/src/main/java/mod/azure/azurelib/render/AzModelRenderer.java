package mod.azure.azurelib.render;

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

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.cache.object.GeoQuad;
import mod.azure.azurelib.cache.object.GeoVertex;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.item.AzItemRendererPipelineContext;
import mod.azure.azurelib.util.client.RenderUtils;

/**
 * AzModelRenderer provides a generic and extensible base class for rendering models by processing hierarchical bone
 * structures recursively. It leverages a rendering pipeline and a layer renderer to facilitate advanced rendering
 * tasks, including layer application and animated texture processing.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> the type of animatable object this renderer supports
 */
public class AzModelRenderer<K, T> {

    private final Matrix4f poseStateCache = new Matrix4f();

    private final Vector3f normalCache = new Vector3f();

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected final AzLayerRenderer<K, T> layerRenderer;

    public AzModelRenderer(AzRendererPipeline<K, T> rendererPipeline, AzLayerRenderer<K, T> layerRenderer) {
        this.layerRenderer = layerRenderer;
        this.rendererPipeline = rendererPipeline;
    }

    /**
     * The actual render method that subtype renderers should override to handle their specific rendering tasks.<br>
     */
    protected void render(AzRendererPipelineContext<K, T> context, boolean isReRender) {
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
    protected void renderRecursively(AzRendererPipelineContext<K, T> context, AzBone bone, boolean isReRender) {
        var buffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var poseStack = context.poseStack();

        poseStack.pushPose();
        RenderUtils.prepMatrixForBone(poseStack, bone);

        context.setVertexConsumer(getOrRefreshRenderBuffer(isReRender, context, bone));

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
    protected void renderCubesOfBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
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
    protected void renderChildBones(AzRendererPipelineContext<K, T> context, AzBone bone, boolean isReRender) {
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
    protected void renderCube(AzRendererPipelineContext<K, T> context, GeoCube cube) {
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

            normalCache.set(quad.normal());
            normalisedPoseState.transform(normalCache);
            var normal = normalCache;

            RenderUtils.fixInvertedFlatCube(cube, normal);
            createVerticesOfQuad(context, quad, poseState, normal);
        }
    }

    /**
     * Applies the {@link GeoQuad Quad's} {@link GeoVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering
     */
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<K, T> context,
        GeoQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        var buffer = context.vertexConsumer();
        var config = rendererPipeline.config();
        var packedOverlay = context.packedOverlay();
        var packedLight = context.packedLight();
        var boneTextureSize = context.computeTextureSize(context.getTextureOverride());
        var entityTextureSize = context.computeTextureSize(
            config.textureLocation(context.currentEntity, context.animatable())
        );

        for (var vertex : quad.vertices()) {
            var position = vertex.position();
            var vector4f = poseState.transform(new Vector4f(position.x(), position.y(), position.z(), 1.0f));

            if (context.getTextureOverride() != null && boneTextureSize != null && entityTextureSize != null) {
                var texU = (vertex.texU() * entityTextureSize.firstInt()) / boneTextureSize.firstInt();
                var texV = (vertex.texV() * entityTextureSize.secondInt()) / boneTextureSize.secondInt();
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
            } else {
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
    }

    /**
     * Provides an override logic for rendering a specific bone. This method allows custom handling of how an individual
     * bone should be rendered.
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
     * @return A boolean indicating whether this override has applied custom rendering logic. If true, the custom
     *         behavior has been applied; otherwise, the default rendering will proceed.
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

    public void handleAnimation(AzAnimator<K, T> animator, T animatable, float partialTick) {
        animator.animate(animatable, partialTick);
    }

    /**
     * Retrieves or refreshes the {@link VertexConsumer} for rendering based on the current buffer state and rendering
     * context. Depending on the type and state of the current {@link VertexConsumer}, this method determines whether to
     * reuse the existing buffer or obtain a fresh one from the {@link MultiBufferSource}.
     *
     * @param context    The rendering context containing information about the current buffer, the buffer source, and
     *                   rendering pipeline data.
     * @param bone       The {@link AzBone} being rendered, which may influence the behavior or context of the buffer
     *                   retrieval.
     * @param renderType The {@link RenderType} specifying the desired render characteristics or pipeline for rendering.
     * @return The appropriate {@link VertexConsumer} for rendering, either the existing buffer or a refreshed/new one.
     */
    public VertexConsumer getOrRefreshBufferRenderType(
        AzItemRendererPipelineContext context,
        AzBone bone,
        RenderType renderType
    ) {
        var currentBuffer = context.multiBufferSource().getBuffer(renderType);
        var bufferSource = context.multiBufferSource();

        if (currentBuffer instanceof BufferBuilder builder) {
            if (isBufferInactive(builder)) {
                return bufferSource.getBuffer(renderType);
            }
        } else if (currentBuffer instanceof OutlineBufferSource.EntityOutlineGenerator outline) {
            if (needsBufferRefresh(outline.delegate)) {
                return new OutlineBufferSource.EntityOutlineGenerator(
                    bufferSource.getBuffer(renderType),
                    255,
                    255,
                    255,
                    255
                );
            }
        } else if (currentBuffer instanceof VertexMultiConsumer.Double pair) {
            var firstBuffer = pair.first;
            var secondBuffer = pair.second;
            boolean firstNeedsRefresh = needsBufferRefresh(firstBuffer);
            boolean secondNeedsRefresh = needsBufferRefresh(secondBuffer);

            if (firstNeedsRefresh || secondNeedsRefresh) {
                return new VertexMultiConsumer.Double(
                    firstNeedsRefresh ? bufferSource.getBuffer(renderType) : firstBuffer,
                    secondNeedsRefresh ? bufferSource.getBuffer(renderType) : secondBuffer
                );
            }
        }
        return currentBuffer;
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
    public VertexConsumer getOrRefreshRenderBuffer(
        boolean isReRender,
        AzRendererPipelineContext<K, T> context,
        AzBone bone
    ) {
        var config = rendererPipeline.config();
        var currentBuffer = context.vertexConsumer();
        var bufferSource = context.multiBufferSource();
        var renderType = context.renderType();

        if (config.boneTextureOverrideProvider(bone) != null) {
            context.setTextureOverride(config.boneTextureOverrideProvider(bone));
        }

        var texture = config.boneTextureOverrideProvider(bone);

        var renderTypeOverride = config.boneRenderTypeOverrideProvider(bone);

        if (texture != null && renderTypeOverride == null) {
            renderTypeOverride = context.getDefaultRenderType(
                context.animatable(),
                texture,
                bufferSource,
                context.partialTick(),
                config.getRenderType(context.currentEntity, context.animatable()),
                config.alpha(context.animatable())
            );
        }

        if (renderTypeOverride != null) {
            currentBuffer = context.multiBufferSource().getBuffer(renderTypeOverride);
        }

        if (isReRender) {
            return currentBuffer;
        }

        if (currentBuffer instanceof BufferBuilder builder) {
            if (isBufferInactive(builder)) {
                return bufferSource.getBuffer(renderType);
            }
        } else if (currentBuffer instanceof OutlineBufferSource.EntityOutlineGenerator outline) {
            if (needsBufferRefresh(outline.delegate)) {
                return new OutlineBufferSource.EntityOutlineGenerator(
                    bufferSource.getBuffer(renderType),
                    255,
                    255,
                    255,
                    255
                );
            }
        } else if (currentBuffer instanceof VertexMultiConsumer.Double pair) {
            var firstBuffer = pair.first;
            var secondBuffer = pair.second;
            boolean firstNeedsRefresh = needsBufferRefresh(firstBuffer);
            boolean secondNeedsRefresh = needsBufferRefresh(secondBuffer);

            if (firstNeedsRefresh || secondNeedsRefresh) {
                return new VertexMultiConsumer.Double(
                    firstNeedsRefresh ? bufferSource.getBuffer(renderType) : firstBuffer,
                    secondNeedsRefresh ? bufferSource.getBuffer(renderType) : secondBuffer
                );
            }
        }
        return currentBuffer;
    }

    /**
     * Determines whether the given {@link VertexConsumer} requires a buffer refresh. This involves checking the
     * specific type of the {@link VertexConsumer} and applying appropriate logic to evaluate its state.
     *
     * @param buffer The {@link VertexConsumer} instance to evaluate.
     * @return {@code true} if the buffer needs to be refreshed; {@code false} otherwise.
     */
    protected boolean needsBufferRefresh(VertexConsumer buffer) {
        if (buffer instanceof BufferBuilder builder) {
            return isBufferInactive(builder);
        } else if (buffer instanceof OutlineBufferSource.EntityOutlineGenerator outline) {
            return needsBufferRefresh(outline.delegate);
        } else if (buffer instanceof VertexMultiConsumer.Double pair) {
            return needsBufferRefresh(pair.first) || needsBufferRefresh(pair.second);
        }
        return false;
    }

    /**
     * Determines if the given {@link BufferBuilder} is inactive. A buffer is considered inactive if it is not currently
     * in the process of building.
     *
     * @param builder The {@link BufferBuilder} instance to check.
     * @return {@code true} if the buffer is inactive (not building); {@code false} otherwise.
     */
    protected boolean isBufferInactive(BufferBuilder builder) {
        return !builder.building;
    }
}
