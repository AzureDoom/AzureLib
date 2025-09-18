package mod.azure.azurelib;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.entity.BlockEntityType;

import mod.azure.azurelib.config.AzureLibConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.platform.FabricAzureLibNetwork;

public final class FabricAzureLibMod implements ModInitializer {

    public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;

    public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLibMod.config = AzureLibMod.registerConfig(AzureLibConfig.class, ConfigFormats.json()).getConfigInstance();
        AzureLib.initialize();
        new FabricAzureLibNetwork();

        Registry.register(
            Registry.BLOCK,
            AzureLib.modResource("lightblock"),
            FabricAzureLibMod.TICKING_LIGHT_BLOCK
        );
        FabricAzureLibMod.TICKING_LIGHT_ENTITY = Registry.register(
            Registry.BLOCK_ENTITY_TYPE,
            AzureLib.MOD_ID + ":lightblock",
            FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, FabricAzureLibMod.TICKING_LIGHT_BLOCK)
                .build(null)
        );

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
