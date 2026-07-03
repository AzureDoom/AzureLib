package mod.azure.azurelib.render.item;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.render.AzRendererPipeline;
import mod.azure.azurelib.render.AzRendererPipelineContext;

/**
 * A specialized subclass of {@link AzRendererPipelineContext} designed for rendering {@link ItemStack} objects.
 * Provides the default rendering context and pipeline for rendering item models within a custom rendering framework.
 * <br>
 * This context delegates rendering operations to its associated {@link AzRendererPipeline} while providing additional
 * configuration and control over the rendering process of an {@link ItemStack}.
 */
public class AzItemRendererPipelineContext extends AzRendererPipelineContext<UUID, ItemStack> {

    private boolean translucent = false;

    private ItemDisplayContext transformType;

    public AzItemRendererPipelineContext(AzRendererPipeline<UUID, ItemStack> rendererPipeline) {
        super(rendererPipeline);
    }

    public ItemDisplayContext getTransformType() {
        return transformType;
    }

    public void setTransformType(ItemDisplayContext transformType) {
        this.transformType = transformType;
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
        Identifier texture,
        float partialTick,
        RenderType defaultRenderType,
        float alpha
    ) {
        return translucent
            ? RenderTypes.entityTranslucentCullItemTarget(texture)
            : defaultRenderType;
    }
}
