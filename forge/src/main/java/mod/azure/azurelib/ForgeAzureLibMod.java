package mod.azure.azurelib;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.network.Networking;

@Mod.EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class ForgeAzureLibMod {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(
        ForgeRegistries.BLOCKS,
        AzureLib.MOD_ID
    );

    public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(
        ForgeRegistries.BLOCK_ENTITIES,
        AzureLib.MOD_ID
    );

    public ForgeAzureLibMod() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AzureLib.initialize();
        AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
        modEventBus.addListener(this::init);
        BLOCKS.register(modEventBus);
        TILE_TYPES.register(modEventBus);
    }

    private void init(FMLCommonSetupEvent event) {
        Networking.PacketRegistry.register();
        ConfigIO.FILE_WATCH_MANAGER.startService();
    }
}
