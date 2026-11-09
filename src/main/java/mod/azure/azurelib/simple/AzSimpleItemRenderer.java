package mod.azure.azurelib.simple;

import mod.azure.azurelib.render.item.AzItemRenderer;
import mod.azure.azurelib.render.item.AzItemRendererConfig;
import net.minecraft.item.ItemStack;

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
