package mod.azure.azurelib.render;

import java.util.Collection;
import java.util.List;
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
    protected void preApplyRenderLayers(AzRendererPipelineContext<K, T> context) {
        for (AzRenderLayer<K, T> renderLayer : renderLayerSupplier.get()) {
            renderLayer.preRender(context);
        }
    }

    /**
     * Calls back to the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer for their
     * {@link AzRenderLayer#renderForBone per-bone} render actions.
     */
    public void applyRenderLayersForBone(
        AzRendererPipelineContext<K, T> context,
        AzBone bone
    ) {
        Collection<AzRenderLayer<K, T>> layers = renderLayerSupplier.get();

        if (layers instanceof List) {
            for (int i = 0, size = layers.size(); i < size; i++) {
                List<AzRenderLayer<K, T>> list = ((List<AzRenderLayer<K, T>>) layers);
                renderLayerForBone(context, list.get(i), bone);
            }
        } else {
            for (AzRenderLayer<K, T> renderLayer : layers) {
                renderLayerForBone(context, renderLayer, bone);
            }
        }
    }

    /**
     * Render the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer
     */
    protected void applyRenderLayers(AzRendererPipelineContext<K, T> context) {
        for (AzRenderLayer<K, T> renderLayer : renderLayerSupplier.get()) {
            AzTranslucencySorter sorter = unsortedSorter(context, renderLayer);

            if (sorter == null) {
                renderLayer.render(context);
                continue;
            }

            sorter.pushUnsorted();

            try {
                renderLayer.render(context);
            } finally {
                sorter.popUnsorted();
            }
        }
    }

    private void renderLayerForBone(AzRendererPipelineContext<K, T> context, AzRenderLayer<K, T> layer, AzBone bone) {
        AzTranslucencySorter sorter = unsortedSorter(context, layer);

        if (sorter == null) {
            layer.renderForBone(context, bone);
            return;
        }

        sorter.pushUnsorted();

        try {
            layer.renderForBone(context, bone);
        } finally {
            sorter.popUnsorted();
        }
    }

    private static AzTranslucencySorter unsortedSorter(
            AzRendererPipelineContext<?, ?> context,
            AzRenderLayer<?, ?> layer
    ) {
        if (
                !layer.sortsTranslucency()
                        && context.multiBufferSource() instanceof AzTranslucencySorter
        ) {

            AzTranslucencySorter sorter =
                    (AzTranslucencySorter) context.multiBufferSource();

            if (sorter.isActive()) {
                return sorter;
            }
        }

        return null;
    }
}
