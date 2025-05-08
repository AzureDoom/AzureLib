package mod.azure.azurelib;

import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.network.AzureLibNetwork;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class AzureLibMod {

	public static AzureLibMod instance;
	public static TestingConfig config;

	public AzureLibMod() {
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AzureLib.initialize();
		modEventBus.addListener(this::init);
		AzureLibMod.config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
		AzureBlocks.BLOCKS.register(modEventBus);
		AzureEntities.TILE_TYPES.register(modEventBus);
	}

	private void init(FMLCommonSetupEvent event) {
		AzureLibNetwork.PacketRegistry.register();
		ConfigIO.FILE_WATCH_MANAGER.startService();
	}

	public class AzureBlocks {
		public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
				AzureLib.MOD_ID);

		public static final RegistryObject<Block> TICKING_LIGHT_BLOCK = BLOCKS.register("lightblock",
				() -> new TickingLightBlock());
	}

	public class AzureEntities {

		public static final DeferredRegister<BlockEntityType<?>> TILE_TYPES = DeferredRegister
				.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AzureLib.MOD_ID);

		public static final RegistryObject<BlockEntityType<TickingLightEntity>> TICKING_LIGHT_ENTITY = TILE_TYPES
				.register("lightblock", () -> BlockEntityType.Builder
						.of(TickingLightEntity::new, AzureBlocks.TICKING_LIGHT_BLOCK.get()).build(null));
	}

	/**
	 * Registers your config class. Config will be immediately loaded upon calling.
	 *
	 * @param cfgClass      Your config class
	 * @param formatFactory File format to be used by this config class. You can use values from {@link ConfigFormats} for example.
	 * @param <CFG>         Config type
	 * @return Config holder containing your config instance. You obtain it by calling {@link ConfigHolder#getConfigInstance()} method.
	 */
	public static <CFG> ConfigHolder<CFG> registerConfig(Class<CFG> cfgClass, IConfigFormatHandler formatFactory) {
		Config cfg = cfgClass.getAnnotation(Config.class);
		if (cfg == null) {
			throw new IllegalArgumentException("Config class must be annotated with '@Config' annotation");
		}
		String id = cfg.id();
		String filename = cfg.filename();
		if (filename.isEmpty()) {
			filename = id;
		}
		String group = cfg.group();
		if (group.isEmpty()) {
			group = id;
		}
		ConfigHolder<CFG> holder = new ConfigHolder<>(cfgClass, id, filename, group, formatFactory);
		ConfigHolder.registerConfig(holder);
		if (cfgClass.getAnnotation(Config.NoAutoSync.class) == null) {
			ConfigIO.FILE_WATCH_MANAGER.addTrackedConfig(holder);
		}
		return holder;
	}
}
