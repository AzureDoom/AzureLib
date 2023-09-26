package mod.azure.azurelib;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.client.screen.ConfigGroupScreen;
import mod.azure.azurelib.client.screen.ConfigScreen;
import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.config.value.ConfigValue;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import mod.azure.azurelib.network.Networking;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber
@Mod(AzureLib.MOD_ID)
public final class AzureLibMod {

	public static AzureLibMod instance;

	public AzureLibMod() {
		instance = this;
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		AzureLib.initialize();
		modEventBus.addListener(this::init);
		modEventBus.addListener(this::clientInit);
		AzureBlocks.BLOCKS.register(modEventBus);
		AzureEntities.TILE_TYPES.register(modEventBus);
        registerConfig(TestingConfig.class, ConfigFormats.yaml());
	}

	/**
	 * Registers your config class. Config will be immediately loaded upon calling.
	 *
	 * @param cfgClass      Your config class
	 * @param formatFactory File format to be used by this config class. You can use values from {@link ConfigFormats} for example.
	 * @return Config holder containing your config instance. You obtain it by calling {@link ConfigHolder#getConfigInstance()} method.
	 * @param <CFG> Config type
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

	/**
	 * You can obtain default config screen based on provided config class.
	 *
	 * @param configClass Your config class
	 * @param previous    Previously open screen
	 * @return Either new config screen or {@code null} when no config exists for the provided class
	 */
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public static Screen getConfigScreen(Class<?> configClass, Screen previous) {
		Config cfg = configClass.getAnnotation(Config.class);
		if (cfg == null) {
			return null;
		}
		String id = cfg.id();
		return getConfigScreen(id, previous);
	}

	/**
	 * You can obtain default config screen based on provided config ID.
	 *
	 * @param configId ID of your config
	 * @param previous Previously open screen
	 * @return Either new config screen or {@code null} when no config exists with the provided ID
	 */
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public static Screen getConfigScreen(String configId, Screen previous) {
		return ConfigHolder.getConfig(configId).map(holder -> {
			Map<String, ConfigValue<?>> valueMap = holder.getValueMap();
			return new ConfigScreen(configId, holder.getConfigId(), valueMap, previous);
		}).orElse(null);
	}

	/**
	 * Obtain group of multiple configs based on group ID. This is useful when you have multiple config files for your mod.
	 *
	 * @param group    Group ID, usually mod ID
	 * @param previous Previously open screen
	 * @return Either new config group screen or null when no config exists under the provided group
	 */
	@OnlyIn(Dist.CLIENT)
	public static Screen getConfigScreenByGroup(String group, Screen previous) {
		List<ConfigHolder<?>> list = ConfigHolder.getConfigsByGroup(group);
		if (list.isEmpty())
			return null;
		return getConfigScreenByGroup(list, group, previous);
	}

	@OnlyIn(Dist.CLIENT)
	private static Screen getConfigScreenByGroup(List<ConfigHolder<?>> group, String groupId, Screen previous) {
		return new ConfigGroupScreen(previous, groupId, group);
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
						return getConfigScreen(list.get(0).getConfigId(), screen);
					}
					return getConfigScreenByGroup(list, modId, screen);
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
