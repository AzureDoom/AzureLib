package mod.azure.azurelib.render.armor;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.*;
import mod.azure.azurelib.render.armor.bone.AzArmorBoneProvider;
import mod.azure.azurelib.render.armor.bone.AzDefaultArmorBoneProvider;
import mod.azure.azurelib.render.layer.AzRenderLayer;

public class AzArmorRendererConfig extends AzRendererConfig<UUID, ItemStack> {

    private final AzArmorBoneProvider boneProvider;

    private AzArmorRendererConfig(
        Supplier<AzAnimator<UUID, ItemStack>> animatorProvider,
        AzArmorBoneProvider boneProvider,
        BiFunction<Entity, ItemStack, ResourceLocation> modelLocationProvider,
        BiFunction<Entity, ItemStack, RenderType> renderTypeProvider,
        List<AzRenderLayer<UUID, ItemStack>> renderLayers,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> renderEntry,
        Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> postRenderEntry,
        BiFunction<Entity, ItemStack, ResourceLocation> textureLocationProvider,
        Function<ItemStack, Float> alphaFunction,
        Function<ItemStack, Float> scaleHeight,
        Function<ItemStack, Float> scaleWidth,
        BiFunction<AzRendererPipeline<UUID, ItemStack>, AzLayerRenderer<UUID, ItemStack>, AzModelRenderer<UUID, ItemStack>> modelRendererProvider,
        Function<AzRendererPipeline<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> pipelineContextFunction,
        Function<AzBone, ResourceLocation> boneTextureOverrideProvider,
        Function<AzBone, RenderType> boneRenderTypeOverrideProvider
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
            scaleWidth,
            boneTextureOverrideProvider,
            boneRenderTypeOverrideProvider
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
        return new Builder((a, b) -> modelLocation, (a, b) -> textureLocation);
    }

    public static Builder builder(
        BiFunction<@Nullable Entity, ItemStack, ResourceLocation> modelLocationProvider,
        BiFunction<@Nullable Entity, ItemStack, ResourceLocation> textureLocationProvider
    ) {
        return new Builder(modelLocationProvider, textureLocationProvider);
    }

    public static class Builder extends AzRendererConfig.Builder<UUID, ItemStack> {

        private AzArmorBoneProvider boneProvider;

        protected Builder(
            BiFunction<@Nullable Entity, ItemStack, ResourceLocation> modelLocationProvider,
            BiFunction<@Nullable Entity, ItemStack, ResourceLocation> textureLocationProvider
        ) {
            super(modelLocationProvider, textureLocationProvider);
            this.boneProvider = new AzDefaultArmorBoneProvider();
            this.modelRendererProvider = (entityRendererPipeline, layer) -> new AzArmorModelRenderer(
                (AzArmorRendererPipeline) entityRendererPipeline,
                layer
            );
            this.pipelineContextFunction = AzArmorRendererPipelineContext::new;
            this.renderTypeProvider = (a, b) -> RenderType.entityCutoutNoCull(textureLocationProvider.apply(a, b));
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

        public Builder setRenderType(BiFunction<Entity, ItemStack, RenderType> renderTypeProvider) {
            this.renderTypeProvider = renderTypeProvider;
            return this;
        }

        @Override
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<UUID, ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder setPrerenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setRenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> renderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setRenderEntry(renderEntry);
        }

        @Override
        public Builder setPostRenderEntry(
            Function<AzRendererPipelineContext<UUID, ItemStack>, AzRendererPipelineContext<UUID, ItemStack>> preRenderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setPostRenderEntry(preRenderEntry);
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

        public Builder setBoneProvider(AzArmorBoneProvider boneProvider) {
            this.boneProvider = boneProvider;
            return this;
        }

        @Override
        public AzArmorRendererConfig build() {
            var baseConfig = super.build();

            return new AzArmorRendererConfig(
                baseConfig::createAnimator,
                boneProvider,
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
