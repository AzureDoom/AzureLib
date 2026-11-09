package mod.azure.azurelib.simple;

import mod.azure.azurelib.render.entity.AzEntityRenderer;
import mod.azure.azurelib.render.entity.AzEntityRendererConfig;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("unused")
public class AzSimpleEntityRenderer<T extends Entity> extends AzEntityRenderer<T> {

    protected final AzSimpleConfig<T> simpleConfig;

    public AzSimpleEntityRenderer(
        EntityRendererProvider.Context context,
        AzSimpleConfig.EntityBuilder<? super T> builder
    ) {
        this(context, AzSimpleConfig.narrow(builder.build()));
    }

    protected AzSimpleEntityRenderer(EntityRendererProvider.Context context, AzSimpleConfig<T> simpleConfig) {
        this(context, simpleConfig, rendererConfigBuilder(simpleConfig).build());
    }

    protected AzSimpleEntityRenderer(
        EntityRendererProvider.Context context,
        AzSimpleConfig<T> simpleConfig,
        AzEntityRendererConfig<T> rendererConfig
    ) {
        super(rendererConfig, context);
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
