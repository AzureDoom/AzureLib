package mod.azure.azurelib.rewrite.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

/**
 * The {@code AzRendererConfig} class is a configuration class used for defining rendering configurations for generic
 * animatable objects. It allows customization of model and texture locations, animators, render layers, and scale
 * factors.
 *
 * @param <T> The type of animatable object this configuration applies to.
 */
public class AzRendererConfig<T> {

    private final Supplier<@Nullable AzAnimator<T>> animatorProvider;

    private final Function<T, ResourceLocation> modelLocationProvider;

    private final Function<T, RenderType> renderTypeFunction;

    private final Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry;

    private final Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> postRenderEntry;

    private final List<AzRenderLayer<T>> renderLayers;

    private final Function<T, ResourceLocation> textureLocationProvider;

    private final float scaleHeight;

    private final float scaleWidth;

    public AzRendererConfig(
        Supplier<AzAnimator<T>> animatorProvider,
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, RenderType> renderTypeFunction,
        List<AzRenderLayer<T>> renderLayers,
        Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry,
        Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> postRenderEntry,
        Function<T, ResourceLocation> textureLocationProvider,
        float scaleHeight,
        float scaleWidth
    ) {
        this.animatorProvider = animatorProvider;
        this.modelLocationProvider = modelLocationProvider;
        this.renderTypeFunction = renderTypeFunction;
        this.renderLayers = Collections.unmodifiableList(renderLayers);
        this.preRenderEntry = preRenderEntry;
        this.postRenderEntry = postRenderEntry;
        this.textureLocationProvider = textureLocationProvider;
        this.scaleHeight = scaleHeight;
        this.scaleWidth = scaleWidth;
    }

    public @Nullable AzAnimator<T> createAnimator() {
        return animatorProvider.get();
    }

    public ResourceLocation modelLocation(T animatable) {
        return modelLocationProvider.apply(animatable);
    }

    public ResourceLocation textureLocation(T animatable) {
        return textureLocationProvider.apply(animatable);
    }

    public RenderType getRenderType(T entity) {
        return renderTypeFunction.apply(entity);
    }

    public List<AzRenderLayer<T>> renderLayers() {
        return renderLayers;
    }

    public AzRendererPipelineContext<T> preRenderEntry(AzRendererPipelineContext<T> animatable) {
        return preRenderEntry.apply(animatable);
    }

    public AzRendererPipelineContext<T> postRenderEntry(AzRendererPipelineContext<T> animatable) {
        return postRenderEntry.apply(animatable);
    }

    public float scaleHeight() {
        return scaleHeight;
    }

    public float scaleWidth() {
        return scaleWidth;
    }

    public static class Builder<T> {

        protected final Function<T, ResourceLocation> modelLocationProvider;

        protected Function<T, RenderType> renderTypeProvider;

        protected final List<AzRenderLayer<T>> renderLayers;

        protected Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry;

        protected Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> postRenderEntry;

        protected final Function<T, ResourceLocation> textureLocationProvider;

        protected Supplier<@Nullable AzAnimator<T>> animatorProvider;

        protected float scaleHeight;

        protected float scaleWidth;

        protected Builder(
            Function<T, ResourceLocation> modelLocationProvider,
            Function<T, ResourceLocation> textureLocationProvider
        ) {
            this.animatorProvider = () -> null;
            this.modelLocationProvider = modelLocationProvider;
            this.renderTypeProvider = $ -> RenderType.entityCutoutNoCull(textureLocationProvider.apply($));
            this.renderLayers = new ObjectArrayList<>();
            this.preRenderEntry = $ -> $;
            this.postRenderEntry = $ -> $;
            this.textureLocationProvider = textureLocationProvider;
            this.scaleHeight = 1;
            this.scaleWidth = 1;
        }

        public Builder<T> setPrerenderEntry(
            Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> preRenderEntry
        ) {
            this.preRenderEntry = preRenderEntry;
            return this;
        }

        public Builder<T> setPostRenderEntry(
            Function<AzRendererPipelineContext<T>, AzRendererPipelineContext<T>> postRenderEntry
        ) {
            this.postRenderEntry = postRenderEntry;
            return this;
        }

        /**
         * Sets the animator provider for the builder. The animator provider is responsible for supplying an instance of
         * {@link AzAnimator} that defines the animation logic for the target object.
         *
         * @param animatorProvider a {@link Supplier} that provides a {@link AzAnimator} instance or null if no custom
         *                         animation logic is required
         * @return the updated {@code Builder} instance for chaining configuration methods
         */
        public Builder<T> setAnimatorProvider(Supplier<@Nullable AzAnimator<T>> animatorProvider) {
            this.animatorProvider = animatorProvider;
            return this;
        }

        /**
         * Adds a {@link AzRenderLayer} to this config, to be called after the main model is rendered each frame
         */
        public Builder<T> addRenderLayer(AzRenderLayer<T> renderLayer) {
            this.renderLayers.add(renderLayer);
            return this;
        }

        public Builder<T> setRenderType(RenderType renderType) {
            this.renderTypeProvider = $ -> renderType;
            return this;
        }

        public Builder<T> setRenderType(Function<T, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        /**
         * Sets the scaling factor uniformly for both width and height dimensions.
         *
         * @param scale the uniform scaling factor to be applied to both width and height
         * @return the {@code Builder} instance for method chaining
         */
        public Builder<T> setScale(float scale) {
            return setScale(scale, scale);
        }

        /**
         * Sets the scaling factors for both width and height.
         *
         * @param scaleWidth  the scaling factor for the width
         * @param scaleHeight the scaling factor for the height
         * @return the updated builder instance for chaining operations
         */
        public Builder<T> setScale(float scaleWidth, float scaleHeight) {
            this.scaleHeight = scaleHeight;
            this.scaleWidth = scaleWidth;
            return this;
        }

        /**
         * Builds and returns a finalized {@link AzRendererConfig} instance with the current configuration settings
         * provided through the builder.
         *
         * @return a new instance of {@link AzRendererConfig} configured with the specified animator provider, model
         *         location provider, texture location provider, render layers, and scale factors.
         */
        public AzRendererConfig<T> build() {
            return new AzRendererConfig<>(
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
        }
    }
}
