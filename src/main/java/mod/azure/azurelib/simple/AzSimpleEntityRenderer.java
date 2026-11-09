package mod.azure.azurelib.simple;

import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.entity.Entity;

@SuppressWarnings("unused")
public class AzSimpleEntityRenderer<T extends Entity> extends AzEntityRenderer<T> {

    protected final AzSimpleConfig<T> simpleConfig;

    public AzSimpleEntityRenderer(AzSimpleConfig.EntityBuilder<? super T> builder) {
        this(AzSimpleConfig.narrow(builder.build()));
    }

    protected AzSimpleEntityRenderer(AzSimpleConfig<T> simpleConfig) {
        this(simpleConfig, rendererConfigBuilder(simpleConfig).build());
    }

    protected AzSimpleEntityRenderer(
        AzSimpleConfig<T> simpleConfig,
        AzEntityRendererConfig<T> rendererConfig
    ) {
        super(rendererConfig);
        this.simpleConfig = simpleConfig;
    }

    public static <T extends Entity> AzEntityRendererConfig.Builder<T> rendererConfigBuilder(AzSimpleConfig<T> config) {
        return AzEntityRendererConfig.builder(config::modelLocation, config::textureLocation)
            .setAnimatorProvider(() -> new AzSimpleEntityAnimator<>(config))
            .setShadowRadius(config.shadowRadius())
            .setScale(config.scaleWidth(), config.scaleHeight());
    }

    public AzSimpleConfig<T> simpleConfig() {
        return simpleConfig;
    }
}
