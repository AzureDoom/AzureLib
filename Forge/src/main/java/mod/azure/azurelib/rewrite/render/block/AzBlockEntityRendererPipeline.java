package mod.azure.azurelib.rewrite.render.block;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.rewrite.render.*;
import mod.azure.azurelib.util.RenderUtils;

/**
 * AzBlockEntityRendererPipeline is a specific implementation of the {@link AzRendererPipeline} tailored for rendering
 * block entities. It manages the rendering pipeline with customized configurations and rendering behavior for block
 * entities, while integrating with the parent pipeline logic.
 *
 * @param <T> The type of {@link TileEntity} that this renderer pipeline is designed to render.
 */
public class AzBlockEntityRendererPipeline<T extends TileEntity> extends AzRendererPipeline<T> {

    private final AzBlockEntityRenderer<T> blockEntityRenderer;

    protected Matrix4f entityRenderTranslations = new Matrix4f();

    protected Matrix4f modelRenderTranslations = new Matrix4f();

    public AzBlockEntityRendererPipeline(
        AzBlockEntityRendererConfig<T> config,
        AzBlockEntityRenderer<T> blockEntityRenderer
    ) {
        super(config);
        this.blockEntityRenderer = blockEntityRenderer;
    }

    @Override
    protected AzBlockEntityRendererPipelineContext<T> createContext(AzRendererPipeline<T> rendererPipeline) {
        return new AzBlockEntityRendererPipelineContext<>(this);
    }

    @Override
    protected AzModelRenderer<T> createModelRenderer(AzLayerRenderer<T> layerRenderer) {
        return new AzBlockEntityModelRenderer<>(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<T> createLayerRenderer(AzRendererConfig<T> config) {
        return new AzLayerRenderer<>(config::renderLayers);
    }

    /**
     * Update the current frame of a {@link AnimatableTexture potentially animated} texture used by this
     * GeoRenderer.<br>
     * This should only be called immediately prior to rendering, and only
     *
     * @see AnimatableTexture#setAndUpdate(ResourceLocation, int)
     */
    @Override
    public void updateAnimatedTextureFrame(T entity) {
        AnimatableTexture.setAndUpdate(config.textureLocation(entity));
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link MatrixStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(AzRendererPipelineContext<T> context, boolean isReRender) {
        MatrixStack poseStack = context.poseStack();
        RenderUtils.copy(this.entityRenderTranslations, poseStack.getLast().getMatrix());

        float scaleWidth = config.scaleWidth(context.animatable());
        float scaleHeight = config.scaleHeight(context.animatable());
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        if (config.alpha(context.animatable()) < 1) {
            context.setAlpha(config.alpha(context.animatable()));
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<T> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    public AzBlockEntityRenderer<T> getRenderer() {
        return blockEntityRenderer;
    }
}
