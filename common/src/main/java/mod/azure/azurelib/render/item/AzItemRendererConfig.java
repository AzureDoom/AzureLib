package mod.azure.azurelib.render.item;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.layer.AzRenderLayer;

/**
 * Configuration class for rendering items using customized settings in an animation framework. Extends
 * {@link AzRendererConfig} specifically for handling {@link ItemStack}. Provides additional settings specific to item
 * rendering, such as GUI lighting and custom offsets.
 */
public class AzItemRendererConfig extends AzRendererConfig<UUID, ItemStack> {

    private final boolean useEntityGuiLighting;

    private final boolean useNewOffset;

    private final Predicate<ItemDisplayContext> shouldAnimateInContext;

    private AzItemRendererConfig(
        Supplier<AzAnimator<UUID, ItemStack>> animatorProvider,
        Function<ItemStack, ResourceLocation> modelLocationProvider,
        Function<ItemStack, RenderType> renderTypeProvider,
        List<AzRenderLayer<UUID, ItemStack>> renderLayers,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> renderEntry,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> postRenderEntry,
        Function<ItemStack, ResourceLocation> textureLocationProvider,
        Function<ItemStack, Float> alphaFunction,
        Function<ItemStack, Float> scaleHeight,
        Function<ItemStack, Float> scaleWidth,
        boolean useEntityGuiLighting,
        boolean useNewOffset,
        Predicate<ItemDisplayContext> shouldAnimateInContext,
        BiFunction<AzRendererPipeline<UUID, ItemStack>, AzLayerRenderer<UUID, ItemStack>, AzModelRenderer<UUID, ItemStack>> modelRendererProvider,
        Function<AzRendererPipeline<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> pipelineContextFunction,
        Function<AzBone, ResourceLocation> boneTextureOverrideProvider,
        Function<AzBone, RenderType> boneRenderTypeOverrideProvider
    ) {
        super(
            animatorProvider,
            (a, b) -> modelLocationProvider.apply(b),
            modelRendererProvider,
            pipelineContextFunction,
            (a, b) -> renderTypeProvider.apply(b),
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
        this.useEntityGuiLighting = useEntityGuiLighting;
        this.useNewOffset = useNewOffset;
        this.shouldAnimateInContext = shouldAnimateInContext;
    }

    public boolean useEntityGuiLighting() {
        return useEntityGuiLighting;
    }

    public boolean useNewOffset() {
        return useNewOffset;
    }

    public boolean shouldAnimateInContext(ItemDisplayContext context) {
        return shouldAnimateInContext.test(context);
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

    public static class Builder extends AzRendererConfig.Builder<UUID, ItemStack> {

        private boolean useEntityGuiLighting;

        private boolean useNewOffset;

        private Predicate<ItemDisplayContext> shouldAnimateInContext;

        protected Builder(
            Function<ItemStack, ResourceLocation> modelLocationProvider,
            Function<ItemStack, ResourceLocation> textureLocationProvider
        ) {
            super((a, b) -> modelLocationProvider.apply(b), (a, b) -> textureLocationProvider.apply(b));
            this.renderTypeProvider = (a, b) -> RenderType.entityCutoutNoCull(textureLocationProvider.apply(b));
            this.useEntityGuiLighting = false;
            this.useNewOffset = false;
            this.shouldAnimateInContext = $ -> true;
            this.modelRendererProvider = (entityRendererPipeline, layer) -> new AzItemModelRenderer(
                (AzItemRendererPipeline) entityRendererPipeline,
                layer
            );
            this.pipelineContextFunction = AzItemRendererPipelineContext::new;
        }

        @Override
        public Builder setBoneRenderTypeOverrideProvider(Function<AzBone, RenderType> boneRenderTypeOverrideProvider) {
            return (Builder) super.setBoneRenderTypeOverrideProvider(boneRenderTypeOverrideProvider);
        }

        @Override
        public Builder setBoneTextureOverrideProvider(Function<AzBone, ResourceLocation> boneTextureOverrideProvider) {
            return (Builder) super.setBoneTextureOverrideProvider(boneTextureOverrideProvider);
        }

        @Override
        public Builder setModelRenderer(
            BiFunction<AzRendererPipeline<UUID, ItemStack>, AzLayerRenderer<UUID, ItemStack>, AzModelRenderer<UUID, ItemStack>> modelRendererProvider
        ) {
            return (Builder) super.setModelRenderer(modelRendererProvider);
        }

        @Override
        public Builder setPipelineContext(
            Function<AzRendererPipeline<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> azRendererPipelineAzRendererPipelineContextFunction
        ) {
            return (Builder) super.setPipelineContext(azRendererPipelineAzRendererPipelineContextFunction);
        }

        @Override
        public Builder addRenderLayer(AzRenderLayer<UUID, ItemStack> renderLayer) {
            return (Builder) super.addRenderLayer(renderLayer);
        }

        public Builder setRenderType(RenderType renderType) {
            this.renderTypeProvider = (a, b) -> renderType;
            return this;
        }

        public Builder setRenderType(Function<ItemStack, RenderType> renderTypeProvider) {
            this.renderTypeProvider = (a, b) -> renderTypeProvider.apply(b);
            return this;
        }

        public Builder setRenderType(BiFunction<Entity, ItemStack, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        @Override
        public Builder setPrerenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry
        ) {
            return (Builder) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setRenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> renderEntry
        ) {
            return (Builder) super.setRenderEntry(renderEntry);
        }

        @Override
        public Builder setPostRenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry
        ) {
            return (Builder) super.setPostRenderEntry(preRenderEntry);
        }

        @Override
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<UUID, ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder setAlpha(Function<ItemStack, Float> alphaFunction) {
            return (Builder) super.setAlpha(alphaFunction);
        }

        @Override
        public Builder setAlpha(float alpha) {
            return (Builder) super.setAlpha(alpha);
        }

        @Override
        public Builder setScale(Function<ItemStack, Float> scaleFunction) {
            return (Builder) super.setScale(scaleFunction);
        }

        @Override
        public Builder setScale(
            Function<ItemStack, Float> scaleHeightFunction,
            Function<ItemStack, Float> scaleWidthFunction
        ) {
            return (Builder) super.setScale(scaleHeightFunction, scaleWidthFunction);
        }

        @Override
        public Builder setScale(float scale) {
            return (Builder) super.setScale(scale);
        }

        @Override
        public Builder setScale(float scaleWidth, float scaleHeight) {
            return (Builder) super.setScale(scaleWidth, scaleHeight);
        }

        public Builder useEntityGuiLighting() {
            this.useEntityGuiLighting = true;
            return this;
        }

        /**
         * @param useNewOffset Determines whether to apply the y offset for a model due to the change in BlockBench
         *                     4.11.
         */
        public Builder useNewOffset(boolean useNewOffset) {
            this.useNewOffset = useNewOffset;
            return this;
        }

        /**
         * Sets the Predicate to determine whether an item should be animated in a specific {@link ItemDisplayContext}.
         *
         * @param shouldAnimateInContext A Predicate that takes an {@link ItemDisplayContext} and returns true if the
         *                               animation should occur in that context; false otherwise.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder setShouldAnimateInContext(Predicate<ItemDisplayContext> shouldAnimateInContext) {
            this.shouldAnimateInContext = shouldAnimateInContext;
            return this;
        }

        /**
         * Disables animation for specific {@link ItemDisplayContext} instances. The provided contexts are added to a
         * set, and animations will not occur in the specified contexts.
         *
         * @param contextToDisable  The primary {@link ItemDisplayContext} in which animations are to be disabled.
         * @param contextsToDisable Additional {@link ItemDisplayContext} instances in which animations are to be
         *                          disabled.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder disableAnimationInContexts(
            ItemDisplayContext contextToDisable,
            ItemDisplayContext... contextsToDisable
        ) {
            var disabledContexts = new HashSet<ItemDisplayContext>();
            disabledContexts.add(contextToDisable);

            if (contextsToDisable.length > 0) {
                disabledContexts.addAll(Arrays.asList(contextsToDisable));
            }

            var finalDisabledContexts = Set.copyOf(disabledContexts);
            this.shouldAnimateInContext = context -> !finalDisabledContexts.contains(context);
            return this;
        }

        /**
         * Enables animation only for the specified {@link ItemDisplayContext} instances. Any contexts not provided in
         * the parameters will have animations disabled.
         *
         * @param contextToEnable  The primary {@link ItemDisplayContext} where animations should be enabled.
         * @param contextsToEnable Additional {@link ItemDisplayContext} instances where animations should be enabled.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder enableAnimationOnlyInContexts(
            ItemDisplayContext contextToEnable,
            ItemDisplayContext... contextsToEnable
        ) {
            var enabledContexts = new HashSet<ItemDisplayContext>();
            enabledContexts.add(contextToEnable);

            if (contextsToEnable.length > 0) {
                enabledContexts.addAll(Arrays.asList(contextsToEnable));
            }

            var finalEnabledContexts = Set.copyOf(enabledContexts);
            this.shouldAnimateInContext = finalEnabledContexts::contains;
            return this;
        }

        public Builder disableAnimationInAllContexts() {
            this.shouldAnimateInContext = context -> false;
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
                baseConfig::renderEntry,
                baseConfig::postRenderEntry,
                baseConfig::textureLocation,
                baseConfig::alpha,
                baseConfig::scaleHeight,
                baseConfig::scaleWidth,
                useEntityGuiLighting,
                useNewOffset,
                shouldAnimateInContext,
                baseConfig::modelRendererProvider,
                baseConfig::pipelineContext,
                baseConfig::boneTextureOverrideProvider,
                baseConfig::boneRenderTypeOverrideProvider
            );
        }
    }
}
