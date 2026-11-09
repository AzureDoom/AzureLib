package mod.azure.azurelib.common.simple;

import net.minecraft.world.item.ItemStack;

import mod.azure.azurelib.common.render.item.AzItemRenderer;
import mod.azure.azurelib.common.render.item.AzItemRendererConfig;

@SuppressWarnings("unused")
public class AzSimpleItemRenderer extends AzItemRenderer {

    protected final AzSimpleConfig<ItemStack> simpleConfig;

    public AzSimpleItemRenderer(AzSimpleConfig.ItemBuilder builder) {
        this(builder.build());
    }

    protected AzSimpleItemRenderer(AzSimpleConfig<ItemStack> simpleConfig) {
        this(simpleConfig, rendererConfigBuilder(simpleConfig).build());
    }

    protected AzSimpleItemRenderer(AzSimpleConfig<ItemStack> simpleConfig, AzItemRendererConfig rendererConfig) {
        super(rendererConfig);
        this.simpleConfig = simpleConfig;
    }

    public static AzItemRendererConfig.Builder rendererConfigBuilder(AzSimpleConfig<ItemStack> config) {
        AzItemRendererConfig.Builder builder = AzItemRendererConfig.builder(
            config::modelLocation,
            config::textureLocation
        )
            .setAnimatorProvider(() -> new AzSimpleItemAnimator(config))
            .useNewOffset(config.newItemOffset())
            .setScale(config.scaleWidth(), config.scaleHeight());

        if (config.separateFirstPersonAnimator()) {
            builder.useSeparateFirstPersonAnimator();
        }

        return builder;
    }

    public AzSimpleConfig<ItemStack> simpleConfig() {
        return simpleConfig;
    }
}
