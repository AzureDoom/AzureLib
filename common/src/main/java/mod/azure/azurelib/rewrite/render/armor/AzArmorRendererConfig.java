package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.*;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneProvider;
import mod.azure.azurelib.rewrite.render.armor.bone.AzDefaultArmorBoneProvider;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;

public class AzArmorRendererConfig extends AzRendererConfig<ItemStack> {

    private final AzArmorBoneProvider boneProvider;

    private AzArmorRendererConfig(
        Supplier<AzAnimator<ItemStack>> animatorProvider,
        AzArmorBoneProvider boneProvider,
        Function<ItemStack, RenderType> renderTypeProvider,
        Function<ItemStack, ResourceLocation> modelLocationProvider,
        List<AzRenderLayer<ItemStack>> renderLayers,
        Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> preRenderEntry,
        Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> renderEntry,
        Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> postRenderEntry,
        Function<ItemStack, ResourceLocation> textureLocationProvider,
        Function<ItemStack, Float> alphaFunction,
        Function<ItemStack, Float> scaleHeight,
        Function<ItemStack, Float> scaleWidth,
        BiFunction<AzRendererPipeline<ItemStack>, AzLayerRenderer<ItemStack>, AzModelRenderer<ItemStack>> modelRendererProvider,
        Function<AzRendererPipeline<ItemStack>, AzRendererPipelineContext<ItemStack>> pipelineContextFunction
    ) {
        super(
            animatorProvider,
            modelLocationProvider,
            modelRendererProvider,
            pipelineContextFunction,
            renderTypeProvider,
            renderLayers,
            preRenderEntry,
            renderEntry,
            postRenderEntry,
            textureLocationProvider,
            alphaFunction,
            scaleHeight,
            scaleWidth
        );
        this.boneProvider = boneProvider;
    }

    public AzArmorBoneProvider boneProvider() {
        return boneProvider;
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

        private AzArmorBoneProvider boneProvider;

        protected Builder(
            Function<ItemStack, ResourceLocation> modelLocationProvider,
            Function<ItemStack, ResourceLocation> textureLocationProvider
        ) {
            super(modelLocationProvider, textureLocationProvider);
            this.boneProvider = new AzDefaultArmorBoneProvider();
            this.modelRendererProvider = (entityRendererPipeline, layer) -> new AzArmorModelRenderer(
                (AzArmorRendererPipeline) entityRendererPipeline,
                layer
            );
            this.pipelineContextFunction = AzArmorRendererPipelineContext::new;
            this.renderTypeProvider = $ -> RenderType.entityTranslucentCull(textureLocationProvider.apply($));
        }

        @Override
        public Builder setModelRenderer(
            BiFunction<AzRendererPipeline<ItemStack>, AzLayerRenderer<ItemStack>, AzModelRenderer<ItemStack>> modelRendererProvider
        ) {
            return (Builder) super.setModelRenderer(modelRendererProvider);
        }

        @Override
        public Builder setPipelineContext(
            Function<AzRendererPipeline<ItemStack>, AzRendererPipelineContext<ItemStack>> azRendererPipelineAzRendererPipelineContextFunction
        ) {
            return (Builder) super.setPipelineContext(azRendererPipelineAzRendererPipelineContextFunction);
        }

        @Override
        public Builder addRenderLayer(AzRenderLayer<ItemStack> renderLayer) {
            return (Builder) super.addRenderLayer(renderLayer);
        }

        public Builder setRenderType(RenderType renderType) {
            this.renderTypeProvider = $ -> renderType;
            return this;
        }

        public Builder setRenderType(Function<ItemStack, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        @Override
        public Builder setPrerenderEntry(
            Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> preRenderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setRenderEntry(
            Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> renderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setRenderEntry(renderEntry);
        }

        @Override
        public Builder setPostRenderEntry(
            Function<AzRendererPipelineContext<ItemStack>, AzRendererPipelineContext<ItemStack>> preRenderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setPostRenderEntry(preRenderEntry);
        }

        @Override
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        public Builder setBoneProvider(AzArmorBoneProvider boneProvider) {
            this.boneProvider = boneProvider;
            return this;
        }

        @Override
        public Builder setAlpha(Function<ItemStack, Float> alphaFunction) {
            return (AzArmorRendererConfig.Builder) super.setAlpha(alphaFunction);
        }

        @Override
        public Builder setAlpha(float alpha) {
            return (AzArmorRendererConfig.Builder) super.setAlpha(alpha);
        }

        @Override
        public Builder setScale(Function<ItemStack, Float> scaleFunction) {
            return (AzArmorRendererConfig.Builder) super.setScale(scaleFunction);
        }

        @Override
        public Builder setScale(
            Function<ItemStack, Float> scaleHeightFunction,
            Function<ItemStack, Float> scaleWidthFunction
        ) {
            return (AzArmorRendererConfig.Builder) super.setScale(scaleHeightFunction, scaleWidthFunction);
        }

        @Override
        public Builder setScale(float scale) {
            return (AzArmorRendererConfig.Builder) super.setScale(scale);
        }

        @Override
        public Builder setScale(float scaleWidth, float scaleHeight) {
            return (AzArmorRendererConfig.Builder) super.setScale(scaleWidth, scaleHeight);
        }

        @Override
        public AzArmorRendererConfig build() {
            var baseConfig = super.build();

            return new AzArmorRendererConfig(
                baseConfig::createAnimator,
                boneProvider,
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
                baseConfig::pipelineContext
            );
        }
    }
}
