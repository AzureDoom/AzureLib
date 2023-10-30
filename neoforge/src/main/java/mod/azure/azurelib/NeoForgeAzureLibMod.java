package mod.azure.azurelib;

import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.network.Networking;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.client.ConfigScreenHandler;
//import net.minecraftforge.eventbus.api.IEventBus;
//import net.minecraftforge.fml.ModContainer;
//import net.minecraftforge.fml.ModList;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
//import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
//import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//import net.minecraftforge.registries.DeferredRegister;
//import net.neoforged.neoforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class NeoForgeAzureLibMod {

	public static NeoForgeAzureLibMod instance;

	public NeoForgeAzureLibMod() {
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AzureLib.initialize();
		modEventBus.addListener(this::init);
		modEventBus.addListener(this::clientInit);
		AzureBlocks.BLOCKS.register(modEventBus);
		AzureEntities.TILE_TYPES.register(modEventBus);
        //AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.yaml());
	}

	private void init(FMLCommonSetupEvent event) {
		Networking.PacketRegistry.register();
		ConfigIO.FILE_WATCH_MANAGER.startService();
	}

	private void clientInit(FMLClientSetupEvent event) {
		Map<String, List<ConfigHolder<?>>> groups = ConfigHolder.getConfigGroupingByGroup();
		ModList modList = ModList.get();
		for (Map.Entry<String, List<ConfigHolder<?>>> entry : groups.entrySet()) {
			String modId = entry.getKey();
			Optional<? extends ModContainer> optional = modList.getModContainerById(modId);
			optional.ifPresent(modContainer -> {
				List<ConfigHolder<?>> list = entry.getValue();
				modContainer.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> {
					if (list.size() == 1) {
						return AzureLibMod.getConfigScreen(list.get(0).getConfigId(), screen);
					}
					return AzureLibMod.getConfigScreenByGroup(list, modId, screen);
				}));
			});
		}
	}

	public class AzureBlocks {
		public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AzureLib.MOD_ID);

		public static final RegistryObject<Block> TICKING_LIGHT_BLOCK = BLOCKS.register("lightblock", () -> new TickingLightBlock());
	}

	public class AzureEntities {

		public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AzureLib.MOD_ID);

		public static final RegistryObject<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES.register("lightblock", () -> BlockEntityType.Builder.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get()).build(null));
	}
}
