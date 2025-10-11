package mod.azure.azurelib.render;

import java.util.Collection;
import java.util.function.Supplier;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.layer.AzRenderLayer;

/**
 * Provides a mechanism to manage and apply multiple render layers for a given animatable context. This class acts as a
 * wrapper for handling collections of render layers and delegates the rendering responsibilities to the individual
 * layers.
 *
 * @param <K> The type of the key used to identify the animatable object. Typically, a UUID for items/entities and Long
 *            for BlockEntities.
 * @param <T> The type of animatable entity or object the render layers apply to.
 */
public class AzLayerRenderer<K, T> {

    private final Supplier<Collection<AzRenderLayer<K, T>>> renderLayerSupplier;

    public AzLayerRenderer(Supplier<Collection<AzRenderLayer<K, T>>> renderLayerSupplier) {
        this.renderLayerSupplier = renderLayerSupplier;
    }

    /**
     * Calls back to the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer for their
     * {@link AzRenderLayer#preRender pre-render} actions.
     */
    protected void preApplyRenderLayers(mod.azure.azurelib.render.AzRendererPipelineContext<K, T> context) {
        for (var renderLayer : renderLayerSupplier.get()) {
            renderLayer.preRender(context);
        }
    }

    /**
     * Calls back to the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer for their
     * {@link AzRenderLayer#renderForBone per-bone} render actions.
     */
    public void applyRenderLayersForBone(
        mod.azure.azurelib.render.AzRendererPipelineContext<K, T> context,
        AzBone bone
    ) {
        for (var renderLayer : renderLayerSupplier.get()) {
            renderLayer.renderForBone(context, bone);
        }
    }

    /**
     * Render the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer
     */
    protected void applyRenderLayers(mod.azure.azurelib.render.AzRendererPipelineContext<K, T> context) {
        for (var renderLayer : renderLayerSupplier.get()) {
            renderLayer.render(context);
        }
    }
}
