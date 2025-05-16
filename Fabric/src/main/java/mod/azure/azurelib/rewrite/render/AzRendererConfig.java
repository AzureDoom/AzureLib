package mod.azure.azurelib.rewrite.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

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

    protected final Supplier<@Nullable AzAnimator<T>> animatorProvider;

    protected final Function<T, ResourceLocation> modelLocationProvider;

    protected final Function<T, RenderType> renderTypeFunction;

    private final UnaryOperator<AzRendererPipelineContext<T>> preRenderEntry;

    private final UnaryOperator<AzRendererPipelineContext<T>> postRenderEntry;

    protected final List<AzRenderLayer<T>> renderLayers;

    protected final Function<T, ResourceLocation> textureLocationProvider;

    private final Function<T, Float> alphaFunction;

    private final Function<T, Float> scaleHeight;

    private final Function<T, Float> scaleWidth;

    public AzRendererConfig(
        Supplier<AzAnimator<T>> animatorProvider,
        Function<T, ResourceLocation> modelLocationProvider,
        Function<T, RenderType> renderTypeFunction,
        List<AzRenderLayer<T>> renderLayers,
        UnaryOperator<AzRendererPipelineContext<T>> preRenderEntry,
        UnaryOperator<AzRendererPipelineContext<T>> postRenderEntry,
        Function<T, ResourceLocation> textureLocationProvider,
        Function<T, Float> alphaFunction,
        Function<T, Float> scaleHeight,
        Function<T, Float> scaleWidth
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
        this.alphaFunction = alphaFunction;
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

    public float alpha(T entity) {
        return alphaFunction.apply(entity);
    }

    public float scaleHeight(T entity) {
        return scaleHeight.apply(entity);
    }

    public float scaleWidth(T entity) {
        return scaleWidth.apply(entity);
    }

    public static class Builder<T> {

        private final Function<T, ResourceLocation> modelLocationProvider;

        protected Function<T, RenderType> renderTypeProvider;

        private final List<AzRenderLayer<T>> renderLayers;

        protected UnaryOperator<AzRendererPipelineContext<T>> preRenderEntry;

        protected UnaryOperator<AzRendererPipelineContext<T>> postRenderEntry;

        protected final Function<T, ResourceLocation> textureLocationProvider;

        protected Supplier<@Nullable AzAnimator<T>> animatorProvider;

        protected Function<T, Float> alphaFunction;

        protected Function<T, Float> scaleHeight;

        protected Function<T, Float> scaleWidth;

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
            this.alphaFunction = $ -> 1.0F;
            this.scaleHeight = $ -> 1.0F;
            this.scaleWidth = $ -> 1.0F;
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

        public Builder<T> setPrerenderEntry(
            UnaryOperator<AzRendererPipelineContext<T>> preRenderEntry
        ) {
            this.preRenderEntry = preRenderEntry;
            return this;
        }

        public Builder<T> setPostRenderEntry(
            UnaryOperator<AzRendererPipelineContext<T>> postRenderEntry
        ) {
            this.postRenderEntry = postRenderEntry;
            return this;
        }

        /**
         * Sets the alpha value provider for the builder. The alpha value determines the opacity level of the rendered
         * object and is calculated dynamically based on the specified function.
         *
         * @param alphaFunction a {@link Function} that takes an object of type {@code T} and returns a {@code Float}
         *                      value representing the alpha (opacity) level, where 0.0 is fully transparent and 1.0 is
         *                      fully opaque
         * @return the updated {@code Builder} instance for chaining configuration methods
         */
        public Builder<T> setAlpha(Function<T, Float> alphaFunction) {
            this.alphaFunction = alphaFunction;
            return this;
        }

        /**
         * Sets the alpha transparency level for the builder, which determines the level of transparency to be applied.
         *
         * @param alpha the alpha transparency value to set, where 0.0 represents fully transparent and 1.0 represents
         *              fully opaque
         * @return the updated {@code Builder} instance for chaining configuration methods
         */
        public Builder<T> setAlpha(float alpha) {
            this.alphaFunction = $ -> alpha;
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
            this.scaleHeight = $ -> scaleHeight;
            this.scaleWidth = $ -> scaleWidth;
            return this;
        }

        /**
         * Sets the scaling function for both the width and height dimensions of the target object. The provided
         * function dynamically calculates scaling factors based on the input object of type {@code T}.
         *
         * @param scaleFunction a {@link Function} that takes an object of type {@code T} and returns a {@code Float}
         *                      value representing the scaling factor to be applied uniformly to both width and height
         * @return the updated {@code Builder} instance for chaining configuration methods
         */
        public Builder<T> setScale(Function<T, Float> scaleFunction) {
            this.scaleHeight = scaleFunction;
            this.scaleWidth = scaleFunction;
            return this;
        }

        /**
         * Sets the scaling functions for height and width dimensions. These functions dynamically calculate scaling
         * factors based on the input object of type {@code T}.
         *
         * @param scaleHeightFunction a {@link Function} that takes an object of type {@code T} and returns a
         *                            {@code Float} representing the scaling factor for the height dimension
         * @param scaleWidthFunction  a {@link Function} that takes an object of type {@code T} and returns a
         *                            {@code Float} representing the scaling factor for the width dimension
         * @return the updated {@code Builder} instance for chaining configuration methods
         */
        public Builder<T> setScale(Function<T, Float> scaleHeightFunction, Function<T, Float> scaleWidthFunction) {
            this.scaleHeight = scaleHeightFunction;
            this.scaleWidth = scaleWidthFunction;
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
                alphaFunction,
                scaleHeight,
                scaleWidth
            );
        }
    }
}
