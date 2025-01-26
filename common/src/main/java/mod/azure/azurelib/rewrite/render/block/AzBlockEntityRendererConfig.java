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
        Function<T, ResourceLocation> textureLocationProvider,
        float scaleHeight,
        float scaleWidth
    ) {
        super(
            animatorProvider,
            modelLocationProvider,
            renderTypeFunction,
            renderLayers,
            textureLocationProvider,
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

        @Override
        public Builder<T> setAnimatorProvider(Supplier<@Nullable AzAnimator<T>> animatorProvider) {
            return (Builder<T>) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public AzBlockEntityRendererConfig<T> build() {
            var baseConfig = super.build();

            return new AzBlockEntityRendererConfig<>(
                baseConfig::createAnimator,
                baseConfig::modelLocation,
                baseConfig::getRenderType,
                baseConfig.renderLayers(),
                baseConfig::textureLocation,
                baseConfig.scaleHeight(),
                baseConfig.scaleWidth()
            );
        }
    }
}
