package mod.azure.azurelib.rewrite.render.item;

import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

/**
 * Configuration class for rendering items using customized settings in an animation framework. Extends
 * {@link AzRendererConfig} specifically for handling {@link ItemStack}. Provides additional settings specific to item
 * rendering, such as GUI lighting and custom offsets.
 */
public class AzItemRendererConfig extends AzRendererConfig<ItemStack> {

    private final boolean useEntityGuiLighting;

    private final boolean useNewOffset;

    private AzItemRendererConfig(
        Supplier<AzAnimator<ItemStack>> animatorProvider,
        Function<ItemStack, ResourceLocation> modelLocationProvider,
        Function<ItemStack, RenderType> renderTypeProvider,
        List<AzRenderLayer<ItemStack>> renderLayers,
        Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> preRenderEntry,
        Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> postRenderEntry,
        Function<ItemStack, ResourceLocation> textureLocationProvider,
        float scaleHeight,
        float scaleWidth,
        boolean useEntityGuiLighting,
        boolean useNewOffset
    ) {
        super(
            animatorProvider,
            modelLocationProvider,
            renderTypeProvider,
            renderLayers,
            preRenderEntry,
            postRenderEntry,
            textureLocationProvider,
            scaleHeight,
            scaleWidth
        );
        this.useEntityGuiLighting = useEntityGuiLighting;
        this.useNewOffset = useNewOffset;
    }

    public boolean useEntityGuiLighting() {
        return useEntityGuiLighting;
    }

    public boolean useNewOffset() {
        return useNewOffset;
    }

    public static Builder builder(
        ResourceLocation modelLocation,
        ResourceLocation textureLocation
    ) {
        return new Builder($ -> modelLocation, $ -> textureLocation);
    }

    public static Builder builder(
        Function<ItemStack, ResourceLocation> modelLocationProvider,
        Function<ItemStack, ResourceLocation> textureLocationProvider
    ) {
        return new Builder(modelLocationProvider, textureLocationProvider);
    }

    public static class Builder extends AzRendererConfig.Builder<ItemStack> {

        private boolean useEntityGuiLighting;

        private boolean useNewOffset;

        protected Builder(
            Function<ItemStack, ResourceLocation> modelLocationProvider,
            Function<ItemStack, ResourceLocation> textureLocationProvider
        ) {
            super(modelLocationProvider, textureLocationProvider);
            this.useEntityGuiLighting = false;
            this.useNewOffset = false;
        }

        @Override
        public Builder addRenderLayer(AzRenderLayer<ItemStack> renderLayer) {
            return (Builder) super.addRenderLayer(renderLayer);
        }

        @Override
        public Builder setPrerenderEntry(Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> preRenderEntry) {
            return (Builder) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        public Builder useEntityGuiLighting() {
            this.useEntityGuiLighting = true;
            return this;
        }

        /**
         * @param useNewOffset Determines whether to apply the y offset for a model due to the change in BlockBench
         *                     4.11.
         */
        public AzRendererConfig.Builder<ItemStack> useNewOffset(boolean useNewOffset) {
            this.useNewOffset = useNewOffset;
            return this;
        }

        @Override
        public AzItemRendererConfig build() {
            var baseConfig = super.build();

            return new AzItemRendererConfig(
                baseConfig::createAnimator,
                baseConfig::modelLocation,
                baseConfig::getRenderType,
                baseConfig.renderLayers(),
                baseConfig::preRenderEntry,
                baseConfig::postRenderEntry,
                baseConfig::textureLocation,
                baseConfig.scaleHeight(),
                baseConfig.scaleWidth(),
                useEntityGuiLighting,
                useNewOffset
            );
        }
    }
}
