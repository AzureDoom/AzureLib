package mod.azure.azurelib.render.item;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.layer.AzRenderLayer;
import mod.azure.azurelib.render.vertex.RenderType;
import mod.azure.azurelib.util.AzCollections;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * Configuration class for rendering items using customized settings in an animation framework. Extends
 * {@link AzRendererConfig} specifically for handling {@link ItemStack}. Provides additional settings specific to item
 * rendering, such as GUI lighting and custom offsets.
 */
public class AzItemRendererConfig extends AzRendererConfig<UUID, ItemStack> {

    private final boolean useEntityGuiLighting;

    private final boolean useNewOffset;

    private final Predicate<AzItemDisplayContext> shouldAnimateInContext;

    private final Function<AzItemDisplayContext, AzItemAnimatorView> animatorViewProvider;

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
        Predicate<AzItemDisplayContext> shouldAnimateInContext,
        Function<AzItemDisplayContext, AzItemAnimatorView> animatorViewProvider,
        BiFunction<AzRendererPipeline<UUID, ItemStack>, AzLayerRenderer<UUID, ItemStack>, AzModelRenderer<UUID, ItemStack>> modelRendererProvider,
        Function<AzRendererPipeline<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> pipelineContextFunction,
        BiFunction<ItemStack, AzBone, ResourceLocation> boneTextureOverrideProvider,
        BiFunction<ItemStack, AzBone, RenderType> boneRenderTypeOverrideProvider
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
        this.animatorViewProvider = animatorViewProvider;
    }

    public boolean useEntityGuiLighting() {
        return useEntityGuiLighting;
    }

    public boolean useNewOffset() {
        return useNewOffset;
    }

    /** Contexts left to vanilla rendering; see {@link Builder#useVanillaRenderingIn}. */
    private Set<AzItemDisplayContext> vanillaContexts = Collections.emptySet();

    /** True if AzureLib draws the item in this context; false leaves it to vanilla, e.g. a flat GUI icon. */
    public boolean rendersInContext(AzItemDisplayContext context) {
        return !vanillaContexts.contains(context);
    }

    public boolean shouldAnimateInContext(AzItemDisplayContext context) {
        return shouldAnimateInContext.test(context);
    }

    /**
     * @return which of the stack's animators renders in the given display context.
     */
    public AzItemAnimatorView animatorView(AzItemDisplayContext context) {
        AzItemAnimatorView view = animatorViewProvider.apply(context);
        return view != null ? view : AzItemAnimatorView.DEFAULT;
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

        private Predicate<AzItemDisplayContext> shouldAnimateInContext;

        private Function<AzItemDisplayContext, AzItemAnimatorView> animatorViewProvider;

        private final Set<AzItemDisplayContext> vanillaContexts = new HashSet<>();

        protected Builder(
            Function<ItemStack, ResourceLocation> modelLocationProvider,
            Function<ItemStack, ResourceLocation> textureLocationProvider
        ) {
            super((a, b) -> modelLocationProvider.apply(b), (a, b) -> textureLocationProvider.apply(b));
            this.renderTypeProvider = (a, b) -> RenderType.entityCutoutNoCull(textureLocationProvider.apply(b));
            this.useEntityGuiLighting = false;
            this.useNewOffset = false;
            this.shouldAnimateInContext = $ -> true;
            this.animatorViewProvider = $ -> AzItemAnimatorView.DEFAULT;
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
        public Builder setAnimatorProvider(Supplier<AzAnimator<UUID, ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder setTranslucencySorting(boolean enabled) {
            return (Builder) super.setTranslucencySorting(enabled);
        }

        @Override
        public Builder setTranslucencySorting(Predicate<RenderType> sortedRenderTypes) {
            return (Builder) super.setTranslucencySorting(sortedRenderTypes);
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
         * Sets the Predicate to determine whether an item should be animated in a specific
         * {@link mod.azure.azurelib.render.item.AzItemDisplayContext}.
         *
         * @param shouldAnimateInContext A Predicate that takes an
         *                               {@link mod.azure.azurelib.render.item.AzItemDisplayContext} and returns true if
         *                               the animation should occur in that context; false otherwise.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder setShouldAnimateInContext(
            Predicate<AzItemDisplayContext> shouldAnimateInContext
        ) {
            this.shouldAnimateInContext = shouldAnimateInContext;
            return this;
        }

        /**
         * Disables animation for specific {@link mod.azure.azurelib.render.item.AzItemDisplayContext} instances. The
         * provided contexts are added to a set, and animations will not occur in the specified contexts.
         *
         * @param contextToDisable  The primary {@link mod.azure.azurelib.render.item.AzItemDisplayContext} in which
         *                          animations are to be disabled.
         * @param contextsToDisable Additional {@link mod.azure.azurelib.render.item.AzItemDisplayContext} instances in
         *                          which animations are to be disabled.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder disableAnimationInContexts(
            AzItemDisplayContext contextToDisable,
            AzItemDisplayContext... contextsToDisable
        ) {
            HashSet<AzItemDisplayContext> disabledContexts =
                new HashSet<AzItemDisplayContext>();
            disabledContexts.add(contextToDisable);

            if (contextsToDisable.length > 0) {
                disabledContexts.addAll(Arrays.asList(contextsToDisable));
            }

            Set<AzItemDisplayContext> finalDisabledContexts = mod.azure.azurelib.util.AzCollections.immutableSet(
                disabledContexts
            );
            this.shouldAnimateInContext = context -> !finalDisabledContexts.contains(context);
            return this;
        }

        /**
         * Enables animation only for the specified {@link mod.azure.azurelib.render.item.AzItemDisplayContext}
         * instances. Any contexts not provided in the parameters will have animations disabled.
         *
         * @param contextToEnable  The primary {@link mod.azure.azurelib.render.item.AzItemDisplayContext} where
         *                         animations should be enabled.
         * @param contextsToEnable Additional {@link mod.azure.azurelib.render.item.AzItemDisplayContext} instances
         *                         where animations should be enabled.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder enableAnimationOnlyInContexts(
            AzItemDisplayContext contextToEnable,
            AzItemDisplayContext... contextsToEnable
        ) {
            HashSet<AzItemDisplayContext> enabledContexts =
                new HashSet<AzItemDisplayContext>();
            enabledContexts.add(contextToEnable);

            if (contextsToEnable.length > 0) {
                enabledContexts.addAll(Arrays.asList(contextsToEnable));
            }

            Set<AzItemDisplayContext> finalEnabledContexts = mod.azure.azurelib.util.AzCollections.immutableSet(
                enabledContexts
            );
            this.shouldAnimateInContext = finalEnabledContexts::contains;
            return this;
        }

        public Builder disableAnimationInAllContexts() {
            this.shouldAnimateInContext = context -> false;
            return this;
        }

        /**
         * Gives the first-person hand contexts their own animator, separate from the one used for third person and
         * every other context. Each animator has its own controllers, bone state and timer, so the two views can play
         * different animations at the same time (for example an aim-down-sights fire in first person and a hip fire in
         * third person). <br>
         * Target a view with {@code AzCommand#sendForItemFirstPerson}, {@code AzCommand#sendForItemThirdPerson} or
         * {@code AzCommand#sendForItem(Entity, ItemStack, AzItemAnimatorView)}; the plain
         * {@code AzCommand#sendForItem(Entity, ItemStack)} still plays on both.
         *
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder useSeparateFirstPersonAnimator() {
            this.animatorViewProvider = AzItemAnimatorView::fromDisplayContext;
            return this;
        }

        /**
         * Sets which of the stack's animators renders in each {@link AzItemDisplayContext}. The default sends every
         * context to {@link AzItemAnimatorView#DEFAULT}; {@link #useSeparateFirstPersonAnimator()} is the common case.
         *
         * @param animatorViewProvider maps a display context to the animator view used to render it.
         * @return The current instance of the {@code Builder} for method chaining.
         */
        public Builder setAnimatorViewProvider(
            Function<AzItemDisplayContext, AzItemAnimatorView> animatorViewProvider
        ) {
            this.animatorViewProvider = animatorViewProvider;
            return this;
        }

        /**
         * Leaves the given contexts to vanilla rendering, so the item uses its normal icon or sprite there. For example
         * {@code useVanillaRenderingIn(AzItemDisplayContext.GUI)} keeps a flat inventory icon while the item is still
         * drawn by AzureLib in hand and on the ground.
         */
        public Builder useVanillaRenderingIn(AzItemDisplayContext first, AzItemDisplayContext... rest) {
            this.vanillaContexts.add(first);
            this.vanillaContexts.addAll(Arrays.asList(rest));
            return this;
        }

        @Override
        public AzItemRendererConfig build() {
            AzRendererConfig<UUID, ItemStack> baseConfig = super.build();

            AzItemRendererConfig config = new AzItemRendererConfig(
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
                animatorViewProvider,
                baseConfig::modelRendererProvider,
                baseConfig::pipelineContext,
                baseConfig::boneTextureOverrideProvider,
                baseConfig::boneRenderTypeOverrideProvider
            );
            config.vanillaContexts = AzCollections.immutableSet(this.vanillaContexts);
            config.copyBaseOptions(baseConfig);
            return config;
        }
    }
}
