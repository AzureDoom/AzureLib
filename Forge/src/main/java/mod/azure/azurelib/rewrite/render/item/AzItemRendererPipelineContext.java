package mod.azure.azurelib.rewrite.render.item;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import mod.azure.azurelib.rewrite.render.AzRendererPipeline;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;

/**
 * A specialized subclass of {@link AzRendererPipelineContext} designed for rendering {@link ItemStack} objects.
 * Provides the default rendering context and pipeline for rendering item models within a custom rendering framework.
 * <br>
 * This context delegates rendering operations to its associated {@link AzRendererPipeline} while providing additional
 * configuration and control over the rendering process of an {@link ItemStack}.
 */
public class AzItemRendererPipelineContext extends AzRendererPipelineContext<ItemStack> {

    private boolean translucent = false;

    public AzItemRendererPipelineContext(AzRendererPipeline<ItemStack> rendererPipeline) {
        super(rendererPipeline);
    }

    /**
     * Sets whether the rendering pipeline should render with a translucent effect or not.
     *
     * @param translucent A boolean value indicating whether to enable or disable translucency. If true, the rendering
     *                    pipeline will apply a translucent effect to rendered elements. If false, it will render with
     *                    an opaque effect.
     */
    public void setTranslucent(boolean translucent) {
        this.translucent = translucent;
    }

    @Override
    public RenderType getDefaultRenderType(
        ItemStack animatable,
        ResourceLocation texture,
        IRenderTypeBuffer bufferSource,
        float partialTick
    ) {
        return translucent
            ? RenderType.getEntityTranslucent(texture)
            : RenderType.getEntityCutout(texture);
    }
}
