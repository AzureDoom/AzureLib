package mod.azure.azurelib.rewrite.render.entity;

import net.minecraft.entity.Entity;

import java.util.Collection;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.render.AzLayerRenderer;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

/**
 * A renderer class responsible for rendering additional entity layers for a particular animatable entity type. It
 * extends functionality from {@link AzLayerRenderer} and enables conditional rendering based on entity states.
 *
 * @param <T> The type of animatable entity this renderer is applied to.
 */
public class AzEntityLayerRenderer<T extends Entity> extends AzLayerRenderer<T> {

    public AzEntityLayerRenderer(Supplier<Collection<AzRenderLayer<T>>> renderLayerSupplier) {
        super(renderLayerSupplier);
    }

    /**
     * Render the various {@link AzRenderLayer RenderLayers} that have been registered to this renderer
     */
    @Override
    public void applyRenderLayers(AzRendererPipelineContext<T> context) {
        T animatable = context.animatable();

        if (!animatable.isSpectator()) {
            super.applyRenderLayers(context);
        }
    }
}
