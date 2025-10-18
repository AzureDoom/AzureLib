package mod.azure.azurelib.render.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.layer.AzRenderLayer;

/**
 * Configures the rendering behavior for custom entities in the game. This extends {@link AzRendererConfig}, adding
 * extra functionality specifically for handling entity death rotations.
 *
 * @param <T> the entity type this configuration applies to, extending {@link Entity}
 */
public class AzEntityRendererConfig<T extends Entity> extends AzRendererConfig<UUID, T> {

    private final Function<T, Float> deathMaxRotationProvider;

    private final Function<T, Float> shadowRadius;

    private AzEntityRendererConfig(
        Supplier<AzAnimator<UUID, T>> animatorProvider,
        Function<T, Float> deathMaxRotationProvider,
        Function<T, Float> shadowRadius,
        Function<T, RenderType> renderTypeFunction,
        Function<T, ResourceLocation> modelLocationProvider,
        List<AzRenderLayer<UUID, T>> renderLayers,
        Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> preRenderEntry,
        Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> renderEntry,
        Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> postRenderEntry,
        Function<T, ResourceLocation> textureLocationProvider,
        Function<T, Float> alphaFunction,
        Function<T, Float> scaleHeight,
        Function<T, Float> scaleWidth,
        BiFunction<AzRendererPipeline<UUID, T>, AzLayerRenderer<UUID, T>, AzModelRenderer<UUID, T>> modelRendererProvider,
        Function<AzRendererPipeline<UUID, T>, AzRendererPipelineContext<UUID, T>> pipelineContextFunction,
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
        this.deathMaxRotationProvider = deathMaxRotationProvider;
        this.shadowRadius = shadowRadius;
    }

    public float getDeathMaxRotation(T entity) {
        return deathMaxRotationProvider.apply(entity);
    }

    public float shadowRadius(T entity) {
        return shadowRadius.apply(entity);
    }

    public static <T extends Entity> Builder<T> builder(
        ResourceLocation modelLocation,
        ResourceLocation textureLocation
    ) {
        return new Builder<>($ -> modelLocation, $ -> textureLocation);
    }

    public static <T extends Entity> Builder<T> builder(
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, ResourceLocation> textureLocationProvider
    ) {
        return new Builder<>(modelLocationProvider, textureLocationProvider);
    }

    public static class Builder<T extends Entity> extends AzRendererConfig.Builder<UUID, T> {

        private Function<T, Float> deathMaxRotationProvider;

        protected Function<T, Float> shadowRadius;

        public Builder(
            Function<T, ResourceLocation> modelLocationProvider,
            Function<T, ResourceLocation> textureLocationProvider
        ) {
            super((a, b) -> modelLocationProvider.apply(b), (a, b) -> textureLocationProvider.apply(b));
            this.modelRendererProvider = (entityRendererPipeline, layer) -> new AzEntityModelRenderer<>(
                (AzEntityRendererPipeline<T>) entityRendererPipeline,
                layer
            );
            this.pipelineContextFunction = AzEntityRendererPipelineContext::new;
            this.renderTypeProvider = (a, b) -> RenderType.entityCutout(textureLocationProvider.apply(b));
            this.deathMaxRotationProvider = $ -> 90F;
            this.shadowRadius = $ -> 0.0F;
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
            BiFunction<AzRendererPipeline<UUID, T>, AzLayerRenderer<UUID, T>, AzModelRenderer<UUID, T>> modelRendererProvider
        ) {
            return (Builder<T>) super.setModelRenderer(modelRendererProvider);
        }

        @Override
        public Builder<T> setPipelineContext(
            Function<AzRendererPipeline<UUID, T>, AzRendererPipelineContext<UUID, T>> azRendererPipelineAzRendererPipelineContextFunction
        ) {
            return (Builder<T>) super.setPipelineContext(azRendererPipelineAzRendererPipelineContextFunction);
        }

        @Override
        public Builder<T> addRenderLayer(AzRenderLayer<UUID, T> renderLayer) {
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
            Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> preRenderEntry
        ) {
            return (Builder<T>) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder<T> setRenderEntry(
            Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> renderEntry
        ) {
            return (Builder<T>) super.setRenderEntry(renderEntry);
        }

        @Override
        public Builder<T> setPostRenderEntry(
            Function<AzRendererPipelineContext<UUID, T>, AzRendererPipelineContext<UUID, T>> preRenderEntry
        ) {
            return (Builder<T>) super.setPostRenderEntry(preRenderEntry);
        }

        @Override
        public Builder<T> setAnimatorProvider(Supplier<@Nullable AzAnimator<UUID, T>> animatorProvider) {
            return (Builder<T>) super.setAnimatorProvider(animatorProvider);
        }

        public Builder<T> setDeathMaxRotation(float angle) {
            this.deathMaxRotationProvider = $ -> angle;
            return this;
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

        /**
         * Sets a provider for the max rotation value for dying entities.<br>
         * You might want to modify this for different aesthetics, such as a
         * {@link net.minecraft.world.entity.monster.Spider} flipping upside down on death.<br>
         * Functionally equivalent to {@link net.minecraft.client.renderer.entity.LivingEntityRenderer#getFlipDegrees}
         */
        public Builder<T> setDeathMaxRotation(Function<T, Float> deathMaxRotationProvider) {
            this.deathMaxRotationProvider = deathMaxRotationProvider;
            return this;
        }

        /**
         * Sets a provider function for the shadow radius of an entity. The shadow radius determines the size of the
         * shadow cast by the entity when rendered. This can be dynamic based on the entity's state.
         *
         * @param shadowRadiusFunction A function that provides the shadow radius value based on the entity. The
         *                             function should return a Float representing the desired shadow radius.
         * @return The current {@code Builder<T>} instance with the shadow radius provider function set, allowing for
         *         method chaining.
         */
        public Builder<T> setShadowRadius(Function<T, Float> shadowRadiusFunction) {
            this.shadowRadius = shadowRadiusFunction;
            return this;
        }

        /**
         * Sets the shadow radius for the builder configuration. This value determines the size of the shadow rendered
         * beneath the entity model.
         *
         * @param shadowRadius the radius of the shadow for the entity
         * @return the current instance of the builder for chaining additional configurations
         */
        public Builder<T> setShadowRadius(float shadowRadius) {
            this.shadowRadius = $ -> shadowRadius;
            return this;
        }

        @Override
        public AzEntityRendererConfig<T> build() {
            var baseConfig = super.build();

            return new AzEntityRendererConfig<>(
                baseConfig::createAnimator,
                deathMaxRotationProvider,
                shadowRadius,
                baseConfig::getRenderType,
                baseConfig::modelLocation,
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
