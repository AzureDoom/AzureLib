package mod.azure.azurelib.rewrite.render.armor;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
import mod.azure.azurelib.rewrite.render.AzRendererPipelineContext;
import mod.azure.azurelib.rewrite.render.armor.bone.AzArmorBoneProvider;
import mod.azure.azurelib.rewrite.render.armor.bone.AzDefaultArmorBoneProvider;
import mod.azure.azurelib.rewrite.render.layer.AzRenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class AzArmorRendererConfig extends AzRendererConfig<ItemStack> {

    private final AzArmorBoneProvider boneProvider;

    private AzArmorRendererConfig(
        Supplier<AzAnimator<ItemStack>> animatorProvider,
        AzArmorBoneProvider boneProvider,
        Function<ItemStack, ResourceLocation> modelLocationProvider,
        Function<ItemStack, RenderType> renderTypeProvider,
        List<AzRenderLayer<ItemStack>> renderLayers,
        UnaryOperator<AzRendererPipelineContext<ItemStack>> preRenderEntry,
        UnaryOperator<AzRendererPipelineContext<ItemStack>> postRenderEntry,
        Function<ItemStack, ResourceLocation> textureLocationProvider,
        Function<ItemStack, Float> alphaFunction,
        Function<ItemStack, Float> scaleHeight,
        Function<ItemStack, Float> scaleWidth
    ) {
        super(
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
            this.renderTypeProvider = $ -> RenderType.armorCutoutNoCull(textureLocationProvider.apply($));
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
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        @Override
        public Builder setPrerenderEntry(UnaryOperator<AzRendererPipelineContext<ItemStack>> preRenderEntry
        ) {
            return (AzArmorRendererConfig.Builder) super.setPrerenderEntry(preRenderEntry);
        }

        @Override
        public Builder setPostRenderEntry(UnaryOperator<AzRendererPipelineContext<ItemStack>> preRenderEntry
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
                baseConfig::postRenderEntry,
                baseConfig::textureLocation,
                baseConfig::alpha,
                baseConfig::scaleHeight,
                baseConfig::scaleWidth
            );
        }
    }
}
