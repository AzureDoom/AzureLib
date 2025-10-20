package mod.azure.azurelib.render.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.layer.AzRenderLayer;

/**
 * The {@code AzBlockEntityRendererConfig} class is a specialized configuration for rendering block entities. It extends
 * the generic {@link AzRendererConfig} and provides additional methods to streamline the creation of configurations
 * specifically for block entity renderers.
 *
 * @param <T> The type of block entity this configuration is tailored for.
 */
public class AzBlockEntityRendererConfig<T extends BlockEntity> extends AzRendererConfig<Long, T> {

    private AzBlockEntityRendererConfig(
        Supplier<AzAnimator<Long, T>> animatorProvider,
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, RenderType> renderTypeFunction,
        List<AzRenderLayer<Long, T>> renderLayers,
        Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> preRenderEntry,
        Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> renderEntry,
        Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> postRenderEntry,
        Function<T, ResourceLocation> textureLocationProvider,
        Function<T, Float> alphaFunction,
        Function<T, Float> scaleHeight,
        Function<T, Float> scaleWidth,
        BiFunction<AzRendererPipeline<Long, T>, AzLayerRenderer<Long, T>, AzModelRenderer<Long, T>> modelRendererProvider,
        Function<AzRendererPipeline<Long, T>, AzRendererPipelineContext<Long, T>> pipelineContextFunction,
        Function<AzBone, ResourceLocation> boneTextureOverrideProvider,
        Function<AzBone, RenderType> boneRenderTypeOverrideProvider
    ) {
        super(
            animatorProvider,
            (a, b) -> modelLocationProvider.apply(b),
            modelRendererProvider,
            pipelineContextFunction,
            (a, b) -> renderTypeFunction.apply(b),
            renderLayers,
            preRenderEntry,
            renderEntry,
            postRenderEntry,
            (a, b) -> textureLocationProvider.apply(b),
            alphaFunction,
            scaleHeight,
            scaleWidth,
            boneTextureOverrideProvider,
            boneRenderTypeOverrideProvider
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

    public static class Builder<T extends BlockEntity> extends AzRendererConfig.Builder<Long, T> {

        protected Builder(
            Function<T, ResourceLocation> modelLocationProvider,
            Function<T, ResourceLocation> textureLocationProvider
        ) {
            super((a, b) -> modelLocationProvider.apply(b), (a, b) -> textureLocationProvider.apply(b));
            this.modelRendererProvider = (entityRendererPipeline, layer) -> new AzBlockEntityModelRenderer<>(
                (AzBlockEntityRendererPipeline<T>) entityRendererPipeline,
                layer
            );
            this.pipelineContextFunction = AzBlockEntityRendererPipelineContext::new;
            this.renderTypeProvider = (a, b) -> RenderType.entityTranslucent(textureLocationProvider.apply(b));
        }

        @Override
        public Builder<T> setBoneRenderTypeOverrideProvider(
            Function<AzBone, RenderType> boneRenderTypeOverrideProvider
        ) {
            return (Builder<T>) super.setBoneRenderTypeOverrideProvider(boneRenderTypeOverrideProvider);
        }

        @Override
        public Builder<T> setBoneTextureOverrideProvider(
            Function<AzBone, ResourceLocation> boneTextureOverrideProvider
        ) {
            return (Builder<T>) super.setBoneTextureOverrideProvider(boneTextureOverrideProvider);
        }

        @Override
        public Builder<T> setModelRenderer(
            BiFunction<AzRendererPipeline<Long, T>, AzLayerRenderer<Long, T>, AzModelRenderer<Long, T>> modelRendererProvider
        ) {
            return (Builder<T>) super.setModelRenderer(modelRendererProvider);
        }

        @Override
        public Builder<T> setPipelineContext(
            Function<AzRendererPipeline<Long, T>, AzRendererPipelineContext<Long, T>> pipelineContextFunction
        ) {
            return (Builder<T>) super.setPipelineContext(pipelineContextFunction);
        }

        @Override
        public Builder<T> addRenderLayer(AzRenderLayer<Long, T> renderLayer) {
            return (Builder<T>) super.addRenderLayer(renderLayer);
        }

        public Builder<T> setRenderType(RenderType renderType) {
            this.renderTypeProvider = (a, b) -> renderType;
            return this;
        }

        public Builder<T> setRenderType(Function<T, RenderType> renderTypeProvider) {
            this.renderTypeProvider = (a, b) -> renderTypeProvider.apply(b);
            return this;
        }

        public Builder<T> setRenderType(BiFunction<Entity, T, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        @Override
        public Builder<T> setPrerenderEntry(
            Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> preRenderEntry
        ) {
            return (Builder<T>) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder<T> setRenderEntry(
            Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> renderEntry
        ) {
            return (Builder<T>) super.setRenderEntry(renderEntry);
        }

        @Override
        public Builder<T> setPostRenderEntry(
            Function<AzRendererPipelineContext<Long, T>, AzRendererPipelineContext<Long, T>> preRenderEntry
        ) {
            return (Builder<T>) super.setPostRenderEntry(preRenderEntry);
        }

        @Override
        public Builder<T> setAnimatorProvider(Supplier<@Nullable AzAnimator<Long, T>> animatorProvider) {
            return (Builder<T>) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder<T> setAlpha(Function<T, Float> alphaFunction) {
            return (Builder<T>) super.setAlpha(alphaFunction);
        }

        @Override
        public Builder<T> setAlpha(float alpha) {
            return (Builder<T>) super.setAlpha(alpha);
        }

        @Override
        public Builder<T> setScale(Function<T, Float> scaleFunction) {
            return (Builder<T>) super.setScale(scaleFunction);
        }

        @Override
        public Builder<T> setScale(Function<T, Float> scaleHeightFunction, Function<T, Float> scaleWidthFunction) {
            return (Builder<T>) super.setScale(scaleHeightFunction, scaleWidthFunction);
        }

        @Override
        public Builder<T> setScale(float scale) {
            return (Builder<T>) super.setScale(scale);
        }

        @Override
        public Builder<T> setScale(float scaleWidth, float scaleHeight) {
            return (Builder<T>) super.setScale(scaleWidth, scaleHeight);
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
                baseConfig::renderEntry,
                baseConfig::postRenderEntry,
                baseConfig::textureLocation,
                baseConfig::alpha,
                baseConfig::scaleHeight,
                baseConfig::scaleWidth,
                baseConfig::modelRendererProvider,
                baseConfig::pipelineContext,
                baseConfig::boneTextureOverrideProvider,
                baseConfig::boneRenderTypeOverrideProvider
            );
        }
    }
}
