package mod.azure.azurelib.rewrite.render.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

/**
 * The {@code AzBlockEntityRendererConfig} class is a specialized configuration for rendering block entities. It extends
 * the generic {@link AzRendererConfig} and provides additional methods to streamline the creation of configurations
 * specifically for block entity renderers.
 *
 * @param <T> The type of block entity this configuration is tailored for.
 */
public class AzBlockEntityRendererConfig<T extends BlockEntity> extends AzRendererConfig<T> {

    private AzBlockEntityRendererConfig(
        Supplier<AzAnimator<T>> animatorProvider,
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, RenderType> renderTypeFunction,
        List<AzRenderLayer<T>> renderLayers,
        Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry,
        Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> postRenderEntry,
        Function<T, ResourceLocation> textureLocationProvider,
        Function<T, Float> alphaFunction,
        Function<T, Float> scaleHeight,
        Function<T, Float> scaleWidth
    ) {
        super(
            animatorProvider,
            modelLocationProvider,
            renderTypeFunction,
            renderLayers,
            preRenderEntry,
            postRenderEntry,
            textureLocationProvider,
            alphaFunction,
            scaleHeight,
            scaleWidth
        );
    }

    public static <T extends BlockEntity> Builder<T> builder(
        ResourceLocation modelLocation,
        ResourceLocation textureLocation
    ) {
        return new Builder<>($ -> modelLocation, $ -> textureLocation);
    }

    public static <T extends BlockEntity> Builder<T> builder(
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, ResourceLocation> textureLocationProvider
    ) {
        return new Builder<>(modelLocationProvider, textureLocationProvider);
    }

    public static class Builder<T extends BlockEntity> extends AzRendererConfig.Builder<T> {

        protected Builder(
            Function<T, ResourceLocation> modelLocationProvider,
            Function<T, ResourceLocation> textureLocationProvider
        ) {
            super(modelLocationProvider, textureLocationProvider);
        }

        @Override
        public Builder<T> addRenderLayer(AzRenderLayer<T> renderLayer) {
            return (Builder<T>) super.addRenderLayer(renderLayer);
        }

        public Builder<T> setRenderType(RenderType renderType) {
            this.renderTypeProvider = $ -> renderType;
            return this;
        }

        public Builder<T> setRenderType(Function<T, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        @Override
        public Builder<T> setPrerenderEntry(
            Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry
        ) {
            return (Builder<T>) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setPostRenderEntry(
            Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry
        ) {
            return (Builder) super.setPostRenderEntry(preRenderEntry);
        }

        @Override
        public Builder<T> setAnimatorProvider(Supplier<@Nullable AzAnimator<T>> animatorProvider) {
            return (Builder<T>) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder<T> setAlpha(Function<T, Float> alphaFunction) {
            return (AzBlockEntityRendererConfig.Builder<T>) super.setAlpha(alphaFunction);
        }

        @Override
        public Builder<T> setAlpha(float alpha) {
            return (AzBlockEntityRendererConfig.Builder<T>) super.setAlpha(alpha);
        }

        @Override
        public Builder<T> setScale(Function<T, Float> scaleFunction) {
            return (AzBlockEntityRendererConfig.Builder) super.setScale(scaleFunction);
        }

        @Override
        public Builder<T> setScale(Function<T, Float> scaleHeightFunction, Function<T, Float> scaleWidthFunction) {
            return (AzBlockEntityRendererConfig.Builder) super.setScale(scaleHeightFunction, scaleWidthFunction);
        }

        @Override
        public Builder<T> setScale(float scale) {
            return (AzBlockEntityRendererConfig.Builder<T>) super.setScale(scale);
        }

        @Override
        public Builder<T> setScale(float scaleWidth, float scaleHeight) {
            return (AzBlockEntityRendererConfig.Builder<T>) super.setScale(scaleWidth, scaleHeight);
        }

        @Override
        public AzBlockEntityRendererConfig<T> build() {
            var baseConfig = super.build();

            return new AzBlockEntityRendererConfig<>(
                baseConfig::createAnimator,
                baseConfig::modelLocation,
                baseConfig::getRenderType,
                baseConfig.renderLayers(),
                baseConfig::preRenderEntry,
                baseConfig::postRenderEntry,
                baseConfig::textureLocation,
                baseConfig::alpha,
                baseConfig::scaleHeight,
                baseConfig::scaleWidth
            );
        }
    }
}
