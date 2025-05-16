package mod.azure.azurelib.rewrite.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.rewrite.model.AzBakedModel;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

/**
 * Abstract base class for defining a rendering pipeline. The {@code AzRendererPipeline} provides a structured framework
 * to handle complex rendering tasks by separating responsibilities into different components, such as layer rendering
 * and model rendering.
 *
 * @param <T> The type of the object to be rendered.
 */
public abstract class AzRendererPipeline<T> implements AzPhasedRenderer<T> {

    protected final AzRendererConfig<T> config;

    private final AzRendererPipelineContext<T> context;

    private final AzLayerRenderer<T> layerRenderer;

    private final AzModelRenderer<T> modelRenderer;

    protected AzRendererPipeline(AzRendererConfig<T> config) {
        this.config = config;
        this.context = createContext(this);
        this.layerRenderer = createLayerRenderer(config);
        this.modelRenderer = createModelRenderer(layerRenderer);
    }

    /**
     * Creates a rendering pipeline context for the specified renderer pipeline. This method is intended to be
     * implemented by subclasses to provide a specific implementation of the {@link AzRendererPipelineContext} for
     * rendering.
     *
     * @param rendererPipeline the renderer pipeline for which the context is to be created
     * @return a new instance of {@link AzRendererPipelineContext} specific to the given renderer pipeline
     */
    protected abstract AzRendererPipelineContext<T> createContext(AzRendererPipeline<T> rendererPipeline);

    /**
     * Creates an instance of {@link AzModelRenderer} using the provided {@link AzLayerRenderer}. This method is part of
     * the rendering pipeline and is responsible for generating a model renderer which can handle hierarchical
     * structures and advanced rendering tasks.
     *
     * @param layerRenderer the {@link AzLayerRenderer} instance used to decorate and handle additional render layers
     *                      within the model rendering process
     * @return a new instance of {@link AzModelRenderer} configured with the provided layer renderer
     */
    protected abstract AzModelRenderer<T> createModelRenderer(AzLayerRenderer<T> layerRenderer);

    /**
     * Creates an instance of {@link AzLayerRenderer} using the provided {@link AzRendererConfig}. This method is
     * responsible for generating a layer renderer configured with the provided rendering configuration, allowing for
     * the management and application of multiple render layers.
     *
     * @param config The configuration object of type {@link AzRendererConfig} that provides the necessary settings and
     *               parameters for the layer renderer.
     * @return A newly created {@link AzLayerRenderer} instance configured based on the specified
     *         {@link AzRendererConfig}.
     */
    protected abstract AzLayerRenderer<T> createLayerRenderer(AzRendererConfig<T> config);

    /**
     * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this
     * GeoRenderer.<br>
     * This should only be called immediately prior to rendering, and only
     *
     * @see AnimatableTexture#setAndUpdate
     */
    protected abstract void updateAnimatedTextureFrame(T animatable);

    /**
     * Initial access point for rendering. It all begins here.<br>
     * All AzureLib renderers should immediately defer their respective default {@code render} calls to this, for
     * consistent handling
     */
    public void render(
        MatrixStack poseStack,
        AzBakedModel model,
        T animatable,
        IRenderTypeBuffer bufferSource,
        RenderType renderType,
        IVertexBuilder buffer,
        float yaw,
        float partialTick,
        int packedLight
    ) {
        renderType = config.getRenderType(animatable);
        context.populate(animatable, model, bufferSource, packedLight, partialTick, poseStack, renderType, buffer);

        poseStack.pushPose();

        preRender(context, false);

        // TODO:
        // if (firePreRenderEvent(poseStack, model, bufferSource, partialTick, packedLight)) {
        layerRenderer.preApplyRenderLayers(context);
        modelRenderer.render(context, false);
        layerRenderer.applyRenderLayers(context);
        postRender(context, false);
        // TODO:
        // firePostRenderEvent(poseStack, model, bufferSource, partialTick, packedLight);
        // }

        poseStack.popPose();

        renderFinal(context);
        doPostRenderCleanup();
    }

    /**
     * Re-renders the provided {@link AzBakedModel}.<br>
     * Usually you'd use this for rendering alternate {@link RenderType} layers or for sub-model rendering whilst inside
     * a {@link AzRenderLayer} or similar
     */
    public void reRender(AzRendererPipelineContext<T> context) {
        MatrixStack poseStack = context.poseStack();

        poseStack.pushPose();

        preRender(context, true);
        modelRenderer.render(context, true);
        postRender(context, true);

        poseStack.popPose();
    }

    /**
     * Call after all other rendering work has taken place, including reverting the {@link MatrixStack}'s state. This
     * method is <u>not</u> called in {@link AzRendererPipeline#reRender re-render}
     */
    protected void renderFinal(AzRendererPipelineContext<T> context) {}

    /**
     * Called after all render operations are completed and the render pass is considered functionally complete.
     * <p>
     * Use this method to clean up any leftover persistent objects stored during rendering or any other post-render
     * maintenance tasks as required
     */
    protected void doPostRenderCleanup() {}

    /**
     * Scales the {@link MatrixStack} in preparation for rendering the model, excluding when re-rendering the model as
     * part of a {@link AzRenderLayer} or external render call.<br>
     * Override and call super with modified scale values as needed to further modify the scale of the model (E.G. child
     * entities)
     */
    protected void scaleModelForRender(
        AzRendererPipelineContext<T> context,
        float widthScale,
        float heightScale,
        boolean isReRender
    ) {
        if (!isReRender && (widthScale != 1 || heightScale != 1)) {
            MatrixStack poseStack = context.poseStack();
            poseStack.scale(widthScale, heightScale, widthScale);
        }
    }

    /**
     * Provides access to the rendering configuration associated with this rendering pipeline.
     *
     * @return An instance of {@link AzRendererConfig} that contains the configuration details for this rendering
     *         pipeline, including animator, model location, texture location, render layers, and scaling parameters.
     */
    public AzRendererConfig<T> config() {
        return config;
    }

    /**
     * Provides access to the rendering pipeline context associated with this rendering pipeline.
     *
     * @return An instance of {@link AzRendererPipelineContext} representing the context for the current rendering
     *         pipeline, containing relevant rendering data and configurations for processing animations and models.
     */
    public AzRendererPipelineContext<T> context() {
        return context;
    }
}
