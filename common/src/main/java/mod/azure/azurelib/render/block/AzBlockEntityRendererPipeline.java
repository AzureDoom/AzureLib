package mod.azure.azurelib.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Matrix4f;

import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.render.*;

/**
 * AzBlockEntityRendererPipeline is a specific implementation of the {@link AzRendererPipeline} tailored for rendering
 * block entities. It manages the rendering pipeline with customized configurations and rendering behavior for block
 * entities, while integrating with the parent pipeline logic.
 *
 * @param <T> The type of {@link BlockEntity} that this renderer pipeline is designed to render.
 */
public class AzBlockEntityRendererPipeline<T extends BlockEntity> extends AzRendererPipeline<Long, T> {

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
    protected AzBlockEntityRendererPipelineContext<T> createContext(AzRendererPipeline<Long, T> rendererPipeline) {
        return (AzBlockEntityRendererPipelineContext<T>) config.pipelineContext(this);
    }

    @Override
    protected AzModelRenderer<Long, T> createModelRenderer(AzLayerRenderer<Long, T> layerRenderer) {
        return config.modelRendererProvider(this, layerRenderer);
    }

    @Override
    protected AzLayerRenderer<Long, T> createLayerRenderer(AzRendererConfig<Long, T> config) {
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
        AnimatableTexture.setAndUpdate(config.textureLocation(context().currentEntity(), entity));
    }

    /**
     * Called before rendering the model to buffer. Allows for render modifications and preparatory work such as scaling
     * and translating.<br>
     * {@link PoseStack} translations made here are kept until the end of the render process
     */
    @Override
    public void preRender(AzRendererPipelineContext<Long, T> context, boolean isReRender) {
        var poseStack = context.poseStack();
        this.entityRenderTranslations.set(poseStack.last().pose());

        var scaleWidth = config.scaleWidth(context.animatable());
        var scaleHeight = config.scaleHeight(context.animatable());
        scaleModelForRender(context, scaleWidth, scaleHeight, isReRender);
        if (config.alpha(context.animatable()) < 1) {
            context.setAlpha(config.alpha(context.animatable()));
        }
        config.preRenderEntry(context);
    }

    @Override
    public void postRender(AzRendererPipelineContext<Long, T> context, boolean isReRender) {
        config.postRenderEntry(context);
    }

    public AzBlockEntityRenderer<T> getRenderer() {
        return blockEntityRenderer;
    }
}
