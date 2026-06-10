package mod.azure.azurelib.common.render;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.cache.object.GeoCube;
import mod.azure.azurelib.common.cache.object.GeoQuad;
import mod.azure.azurelib.common.cache.object.GeoVertex;
import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.render.item.AzItemRendererPipelineContext;
import mod.azure.azurelib.common.util.client.RenderUtils;

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

    private final Matrix4f savedPoseScratch = new Matrix4f();

    private final Matrix3f savedNormalScratch = new Matrix3f();

    private final AzRendererPipeline<K, T> rendererPipeline;

    protected final AzLayerRenderer<K, T> layerRenderer;

    private IntIntPair entityTextureSize;

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
                context.renderColor()
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

        var lastEntry = poseStack.last();
        savedPoseScratch.set(lastEntry.pose());
        savedNormalScratch.set(lastEntry.normal());

        for (var cube : bone.getCubes()) {
            renderCube(context, cube);
            lastEntry.pose().set(savedPoseScratch);
            lastEntry.normal().set(savedNormalScratch);
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

        var size = cube.size();
        var isFlat = size.x() == 0 || size.y() == 0 || size.z() == 0;

        for (var quad : cube.quads()) {
            if (quad == null) {
                continue;
            }

            normalScratch.set(quad.normal());
            normalisedPoseState.transform(normalScratch);
            var normal = normalScratch;

            if (isFlat) {
                RenderUtils.fixInvertedFlatCube(cube, normal);
            }
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
        var color = context.renderColor();
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
                buffer.addVertex(
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    -1,
                    vertex.texU() * uScale,
                    vertex.texV() * vScale,
                    packedOverlay,
                    packedLight,
                    nx,
                    ny,
                    nz
                );
            } else {
                buffer.addVertex(
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    color,
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

    public void handleAnimation(AzAnimator<?, T> animator, T animatable, float partialTick) {
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
        var animatable = context.animatable();

        var texture = config.boneTextureOverrideProvider(animatable, bone);

        if (texture != null) {
            context.setTextureOverride(texture);
        }

        var renderTypeOverride = config.boneRenderTypeOverrideProvider(animatable, bone);

        if (texture != null && renderTypeOverride == null) {
            renderTypeOverride = context.getDefaultRenderType(
                context.animatable(),
                texture,
                bufferSource,
                context.partialTick(),
                config.getRenderType(context.currentEntity(), context.animatable()),
                config.alpha(context.animatable())
            );
        }

        if (renderTypeOverride != null) {
            currentBuffer = context.multiBufferSource().getBuffer(renderTypeOverride);
        }

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
    protected boolean needsBufferRefresh(VertexConsumer buffer) {
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
