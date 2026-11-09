package mod.azure.azurelib.common.simple;

import net.minecraft.world.level.block.entity.BlockEntity;

import mod.azure.azurelib.common.render.block.AzBlockEntityRenderer;
import mod.azure.azurelib.common.render.block.AzBlockEntityRendererConfig;

@SuppressWarnings("unused")
public class AzSimpleBlockRenderer<T extends BlockEntity> extends AzBlockEntityRenderer<T> {

    protected final AzSimpleConfig<T> simpleConfig;

    public AzSimpleBlockRenderer(AzSimpleConfig.BlockBuilder<? super T> builder) {
        this(AzSimpleConfig.narrow(builder.build()));
    }

    protected AzSimpleBlockRenderer(AzSimpleConfig<T> simpleConfig) {
        this(simpleConfig, rendererConfigBuilder(simpleConfig).build());
    }

    protected AzSimpleBlockRenderer(AzSimpleConfig<T> simpleConfig, AzBlockEntityRendererConfig<T> rendererConfig) {
        super(rendererConfig);
        this.simpleConfig = simpleConfig;
    }

    public static <T extends BlockEntity> AzBlockEntityRendererConfig.Builder<T> rendererConfigBuilder(
        AzSimpleConfig<T> config
    ) {
        return AzBlockEntityRendererConfig.builder(config::modelLocation, config::textureLocation)
            .setAnimatorProvider(() -> new AzSimpleBlockAnimator<>(config))
            .setScale(config.scaleWidth(), config.scaleHeight());
    }

    public AzSimpleConfig<T> simpleConfig() {
        return simpleConfig;
    }
}
