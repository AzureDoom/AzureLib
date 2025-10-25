package mod.azure.azurelib;

import mod.azure.azurelib.config.TestingConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.*;

import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.platform.FabricAzureLibNetwork;

public final class FabricAzureLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
        AzureLib.initialize();
        new FabricAzureLibNetwork();

        ServerLifecycleEvents.SERVER_STOPPING.register((server) -> ConfigIO.FILE_WATCH_MANAGER.stopService());
    }

    private static <T extends Entity> EntityType<T> mob(
        String id,
        EntityType.EntityFactory<T> factory,
        float height,
        float width
    ) {
        final var type = FabricEntityTypeBuilder.create(MobCategory.MONSTER, factory)
            .dimensions(
                EntityDimensions.scalable(height, width)
            )
            .fireImmune()
            .trackedUpdateRate(1)
            .trackRangeBlocks(
                90
            )
            .build();
        Registry.register(Registry.ENTITY_TYPE, AzureLib.modResource(id), type);

        return type;
    }
}
