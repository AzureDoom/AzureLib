package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3f;
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

    private final Vector3f normalScratch = new Vector3f();

    private final Vector4f quadPosition = new Vector4f();

    private final Matrix3f normalStateCache = new Matrix3f();

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected final AzLayerRenderer<K, T> layerRenderer;

    private IntIntPair entityTextureSize;

    /**
     * The buffer and render type the current render pass draws bones into when they have no override. Bones with a
     * texture or render type override draw into their own buffer, and the bones after them return to this one, so an
     * override never leaks into sibling or child bones.
     */
    @Nullable
    private VertexConsumer passBuffer;

    @Nullable
    private RenderType passRenderType;

    /**
     * Whether an override bone has requested its own buffer since the pass buffer was last used. In 1.20.1 the buffer
     * source reuses one shared {@link BufferBuilder} for every non-fixed render type, so that request re-points the
     * pass buffer's builder at the override's render type; it has to be switched back before the pass buffer is used
     * again.
     */
    private boolean passBufferDisplaced;

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
        var previousPassBuffer = this.passBuffer;
        var previousPassRenderType = this.passRenderType;
        var previousPassBufferDisplaced = this.passBufferDisplaced;
        var previousTextureOverride = context.getTextureOverride();

        this.passBuffer = context.vertexConsumer();
        this.passRenderType = context.renderType();
        this.passBufferDisplaced = false;

        try {
            for (var bone : model.getTopLevelBones()) {
                renderRecursively(context, bone, isReRender);
            }
        } finally {
            if (this.passBuffer != null) {
                restorePassBuffer(context.multiBufferSource());
                context.setVertexConsumer(this.passBuffer);
            }

            context.setTextureOverride(previousTextureOverride);
            this.passBuffer = previousPassBuffer;
            this.passRenderType = previousPassRenderType;
            this.passBufferDisplaced = previousPassBufferDisplaced;
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

        var cubes = bone.getCubes();

        for (int i = 0, size = cubes.size(); i < size; i++) {
            renderCube(context, cubes.get(i));
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

        var children = bone.getChildBones();

        for (int i = 0, size = children.size(); i < size; i++) {
            renderRecursively(context, children.get(i), isReRender);
        }
    }

    /**
     * Renders an individual {@link GeoCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<K, T> context, GeoCube cube) {
        var last = context.poseStack().last();
        var transform = cube.transform();
        var poseState = poseStateCache.set(last.pose());
        Matrix3f normalisedPoseState;

        if (transform.identity()) {
            normalisedPoseState = last.normal();
        } else {
            poseState.mul(transform.pose());
            normalisedPoseState = normalStateCache.set(last.normal()).mul(transform.normal());
        }

        var normalFlips = cube.normalFlips();
        var quadFilter = context.getTextureOverride() == null ? context.quadFilter() : null;

        for (var quad : cube.quads()) {
            if (quad == null || (quadFilter != null && !passes(quad, quadFilter))) {
                continue;
            }

            normalScratch.set(quad.normal());
            normalisedPoseState.transform(normalScratch);
            var normal = normalScratch;

            RenderUtils.fixInvertedFlatCube(normalFlips, normal);
            createVerticesOfQuad(context, quad, poseState, normal);
        }
    }

    private static boolean passes(GeoQuad quad, AzQuadFilter filter) {
        var vertices = quad.vertices();
        float minU = vertices[0].texU(), maxU = minU, minV = vertices[0].texV(), maxV = minV;

        for (int i = 1; i < vertices.length; i++) {
            var u = vertices[i].texU();
            var v = vertices[i].texV();
            minU = Math.min(minU, u);
            maxU = Math.max(maxU, u);
            minV = Math.min(minV, v);
            maxV = Math.max(maxV, v);
        }

        return filter.test(minU, minV, maxU, maxV);
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
        var packedOverlay = context.packedOverlay();
        var packedLight = context.packedLight();
        var textureOverride = context.getTextureOverride();
        var boneTextureSize = textureOverride != null ? context.computeTextureSize(textureOverride) : null;
        boolean useOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;
        float uScale = useOverride ? (float) entityTextureSize.firstInt() / boneTextureSize.firstInt() : 1f;
        float vScale = useOverride ? (float) entityTextureSize.secondInt() / boneTextureSize.secondInt() : 1f;
        float nx = normal.x(), ny = normal.y(), nz = normal.z();

        for (var vertex : quad.vertices()) {
            var position = vertex.position();
            var vector4f = poseState.transform(quadPosition.set(position.x(), position.y(), position.z(), 1.0f));
            if (useOverride) {
                buffer.vertex(
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    context.red(),
                    context.green(),
                    context.blue(),
                    context.alpha(),
                    vertex.texU() * uScale,
                    vertex.texV() * vScale,
                    packedOverlay,
                    packedLight,
                    nx,
                    ny,
                    nz
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
                    nx,
                    ny,
                    nz
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
        var bufferSource = context.multiBufferSource();
        var animatable = context.animatable();

        var texture = config.boneTextureOverrideProvider(animatable, bone);
        var renderTypeOverride = config.boneRenderTypeOverrideProvider(bone);

        context.setTextureOverride(texture);

        if (texture != null && renderTypeOverride == null) {
            var baseTexture = config.textureLocation(context.currentEntity(), animatable);
            var baseRenderType = config.getRenderType(context.currentEntity(), animatable);

            renderTypeOverride = context.getDefaultRenderType(
                animatable,
                texture,
                bufferSource,
                context.partialTick(),
                retargetRenderType(baseRenderType, baseTexture, texture),
                config.alpha(animatable)
            );
        }

        if (renderTypeOverride != null) {
            passBufferDisplaced = true;
            return bufferSource.getBuffer(renderTypeOverride);
        }

        var buffer = passBuffer != null ? passBuffer : context.vertexConsumer();
        var renderType = passRenderType != null ? passRenderType : context.renderType();

        if (buffer == null || renderType == null) {
            return buffer;
        }

        if (passBuffer != null) {
            restorePassBuffer(bufferSource);
        }

        var refreshed = refreshBuffer(buffer, bufferSource, renderType);

        if (refreshed != buffer && passBuffer != null) {
            passBuffer = refreshed;
        }

        return refreshed;
    }

    /**
     * If an override bone took the shared buffer over for its own render type, switches it back to the pass's render
     * type. The pass buffer holds the same {@link BufferBuilder} instance, so asking the buffer source for the pass's
     * render type is enough: it ends the override's batch and restarts the shared builder for this pass, and every
     * wrapper around it (outline, foil, sprite) keeps working.
     */
    private void restorePassBuffer(MultiBufferSource bufferSource) {
        if (passBufferDisplaced && passRenderType != null) {
            bufferSource.getBuffer(passRenderType);
        }

        passBufferDisplaced = false;
    }

    /**
     * Returns {@code buffer}, or a fresh buffer for {@code renderType} in its place if the batch it was writing to has
     * been closed.
     */
    protected VertexConsumer refreshBuffer(
        VertexConsumer buffer,
        MultiBufferSource bufferSource,
        RenderType renderType
    ) {
        if (buffer instanceof BufferBuilder builder) {
            if (isBufferInactive(builder)) {
                return bufferSource.getBuffer(renderType);
            }
        } else if (buffer instanceof OutlineBufferSource.EntityOutlineGenerator outline) {
            if (needsBufferRefresh(outline.delegate)) {
                return new OutlineBufferSource.EntityOutlineGenerator(
                    bufferSource.getBuffer(renderType),
                    255,
                    255,
                    255,
                    255
                );
            }
        } else if (buffer instanceof VertexMultiConsumer.Double pair) {
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

        return buffer;
    }

    /**
     * Returns the equivalent of {@code renderType} for a different texture, used for bones with a texture override.
     * <p>
     * Render types can't be re-pointed at another texture directly, so this recognizes the standard entity render types
     * and rebuilds the matching one for {@code newTexture}. Anything else falls back to
     * {@link RenderType#entityCutout}. For full control over an overridden bone's render type, use the bone render type
     * override provider instead, which takes precedence over this.
     * </p>
     *
     * @param renderType  the render type configured for the model, built for {@code baseTexture}
     * @param baseTexture the model's main texture
     * @param newTexture  the texture the bone should render with
     */
    protected RenderType retargetRenderType(
        @Nullable RenderType renderType,
        @Nullable ResourceLocation baseTexture,
        ResourceLocation newTexture
    ) {
        if (newTexture.equals(baseTexture)) {
            return renderType != null ? renderType : RenderType.entityCutout(newTexture);
        }

        if (renderType != null && baseTexture != null) {
            if (renderType == RenderType.entityCutoutNoCull(baseTexture)) {
                return RenderType.entityCutoutNoCull(newTexture);
            }

            if (renderType == RenderType.entityTranslucent(baseTexture)) {
                return RenderType.entityTranslucent(newTexture);
            }

            if (renderType == RenderType.entityTranslucentCull(baseTexture)) {
                return RenderType.entityTranslucentCull(newTexture);
            }

            if (renderType == RenderType.entityTranslucentEmissive(baseTexture)) {
                return RenderType.entityTranslucentEmissive(newTexture);
            }

            if (renderType == RenderType.entitySolid(baseTexture)) {
                return RenderType.entitySolid(newTexture);
            }

            if (renderType == RenderType.armorCutoutNoCull(baseTexture)) {
                return RenderType.armorCutoutNoCull(newTexture);
            }
        }

        return RenderType.entityCutout(newTexture);
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

    public void cacheTexture(AzRendererPipelineContext<K, T> context) {
        this.entityTextureSize = context.computeTextureSize(
            rendererPipeline.config().textureLocation(context.currentEntity(), context.animatable())
        );
    }

    public void clearCacheTexture() {
        this.entityTextureSize = null;
    }
}
