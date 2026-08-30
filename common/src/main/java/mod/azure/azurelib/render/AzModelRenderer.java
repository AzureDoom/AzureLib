package mod.azure.azurelib.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.cache.object.AzCube;
import mod.azure.azurelib.cache.object.AzQuad;
import mod.azure.azurelib.cache.object.AzVertex;
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
     * Renders the {@link AzCube GeoCubes} associated with a given {@link AzBone}
     */
    protected void renderCubesOfBone(AzRendererPipelineContext<K, T> context, AzBone bone) {
        if (bone.isHidden() || context.vertexConsumer() == null) {
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
     * Renders an individual {@link AzCube}.<br>
     * This tends to be called recursively from something like {@link AzModelRenderer#renderCubesOfBone}
     */
    protected void renderCube(AzRendererPipelineContext<K, T> context, AzCube cube) {
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

            normalScratch.set(quad.normal());
            normalisedPoseState.transform(normalScratch);
            var normal = normalScratch;

            RenderUtils.fixInvertedFlatCube(cube, normal);
            createVerticesOfQuad(context, quad, poseState, normal);
        }
    }

    /**
     * Applies the {@link AzQuad Quad's} {@link AzVertex vertices} to the given {@link VertexConsumer buffer} for
     * rendering
     */
    protected void createVerticesOfQuad(
        AzRendererPipelineContext<K, T> context,
        AzQuad quad,
        Matrix4f poseState,
        Vector3f normal
    ) {
        var buffer = context.vertexConsumer();
        if (buffer == null) {
            return;
        }
        var color = context.renderColor();
        var packedOverlay = context.packedOverlay();
        var packedLight = context.packedLight();
        var textureOverride = context.getTextureOverride();
        var boneTextureSize = context.computeTextureSize(textureOverride);
        boolean useOverride = textureOverride != null && boneTextureSize != null && entityTextureSize != null;
        float nx = normal.x(), ny = normal.y(), nz = normal.z();

        for (var vertex : quad.vertices()) {
            var position = vertex.position();
            var vector4f = poseState.transform(quadPosition.set(position.x(), position.y(), position.z(), 1.0f));

            if (useOverride) {
                var texU = (vertex.texU() * entityTextureSize.firstInt()) / boneTextureSize.firstInt();
                var texV = (vertex.texV() * entityTextureSize.secondInt()) / boneTextureSize.secondInt();

                putFull(
                    buffer,
                    vector4f.x(),
                    vector4f.y(),
                    vector4f.z(),
                    color,
                    texU,
                    texV,
                    packedOverlay,
                    packedLight,
                    nx,
                    ny,
                    nz
                );
            } else {
                putFull(
                    buffer,
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
     * 26.2 removed the all-in-one {@code VertexConsumer#addVertex(x, y, z, color, u, v, overlay, light, nx, ny, nz)}
     * convenience overload in favour of a builder-style chain; this restores a single call site for it.
     */
    private static void putFull(
        VertexConsumer buffer,
        float x,
        float y,
        float z,
        int color,
        float u,
        float v,
        int overlay,
        int light,
        float normalX,
        float normalY,
        float normalZ
    ) {
        buffer.addVertex(x, y, z)
            .setColor(color)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(normalX, normalY, normalZ);
    }

    /**
     * Override method for rendering a specific bone. This method can be customized to apply specific transformations,
     * modify the bone's render properties, or override its rendering behavior entirely.
     *
     * @param poseStack     The pose stack used for handling transformations (rotation, scaling, and translation).
     * @param bone          The bone that is being rendered.
     * @param buffer        The vertex consumer buffer used for writing vertex data during rendering.
     * @param partialTick   The partial tick progress for interpolating between frames.
     * @param packedLight   The packed light value for the rendered bone.
     * @param packedOverlay The packed overlay value for the rendered bone.
     * @param colour        The color modifier for the rendered output.
     * @return A boolean indicating whether the bone's rendering behavior has been overridden successfully.
     */
    @SuppressWarnings("unused")
    public boolean boneRenderOverride(
        PoseStack poseStack,
        AzBone bone,
        AzBufferSource bufferSource,
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

    public VertexConsumer getOrRefreshBufferRenderType(
        AzItemRendererPipelineContext context,
        AzBone bone,
        RenderType renderType
    ) {
        return context.multiBufferSource().getBuffer(renderType);
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

        var textureOverride = config.boneTextureOverrideProvider(bone);
        var renderTypeOverride = config.boneRenderTypeOverrideProvider(bone);

        context.setTextureOverride(textureOverride);

        if (isReRender && textureOverride == null && renderTypeOverride == null) {
            return context.vertexConsumer();
        }

        RenderType activeRenderType;

        if (renderTypeOverride != null) {
            activeRenderType = renderTypeOverride;
        } else if (textureOverride != null) {
            activeRenderType = context.getDefaultRenderType(
                context.animatable(),
                textureOverride,
                bufferSource,
                context.partialTick(),
                config.getRenderType(context.currentEntity(), context.animatable()),
                config.alpha(context.animatable())
            );
        } else {
            activeRenderType = context.renderType();
        }

        if (activeRenderType == null) {
            return null;
        }

        return bufferSource.getBuffer(activeRenderType);
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
