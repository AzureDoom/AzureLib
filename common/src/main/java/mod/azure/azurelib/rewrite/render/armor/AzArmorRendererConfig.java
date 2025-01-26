package mod.azure.azurelib.rewrite.render.armor;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.render.AzRendererConfig;
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
        Function<ItemStack, ResourceLocation> textureLocationProvider,
        float scaleHeight,
        float scaleWidth
    ) {
        super(
            animatorProvider,
            modelLocationProvider,
            renderTypeProvider,
            renderLayers,
            textureLocationProvider,
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

        @Override
        public Builder setAnimatorProvider(Supplier<@Nullable AzAnimator<ItemStack>> animatorProvider) {
            return (Builder) super.setAnimatorProvider(animatorProvider);
        }

        public Builder setBoneProvider(AzArmorBoneProvider boneProvider) {
            this.boneProvider = boneProvider;
            return this;
        }

        public Builder setRenderType(RenderType renderType) {
            this.renderTypeProvider = $ -> renderType;
            return this;
        }

        @Override
        public AzArmorRendererConfig build() {
            var baseConfig = super.build();

            return new AzArmorRendererConfig(
                baseConfig::createAnimator,
                boneProvider,
                renderTypeProvider,
                baseConfig::modelLocation,
                baseConfig.renderLayers(),
                baseConfig::textureLocation,
                baseConfig.scaleHeight(),
                baseConfig.scaleWidth()
            );
        }
    }
}
