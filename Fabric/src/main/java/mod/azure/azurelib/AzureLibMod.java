package mod.azure.azurelib;

import java.util.List;
import java.util.Map;

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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class AzureLibMod implements ModInitializer {

	public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;
	public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();
	public static TestingConfig config;

	@Override
	public void onInitialize() {
		ConfigIO.FILE_WATCH_MANAGER.startService();
		AzureLib.initialize();
		Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(AzureLib.MOD_ID, "lightblock"), TICKING_LIGHT_BLOCK);
		TICKING_LIGHT_ENTITY = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, AzureLib.MOD_ID + ":lightblock", FabricBlockEntityTypeBuilder.create(TickingLightEntity::new, TICKING_LIGHT_BLOCK).build(null));
		config = AzureLibMod.registerConfig(TestingConfig.class, ConfigFormats.json()).getConfigInstance();
		ServerLifecycleEvents.SERVER_STOPPING.register((server) -> {
			ConfigIO.FILE_WATCH_MANAGER.stopService();
		});
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
	@Environment(EnvType.CLIENT)
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
	@Environment(EnvType.CLIENT)
	public static Screen getConfigScreen(String configId, Screen previous) {
		return ConfigHolder.getConfig(configId).map(holder -> getConfigScreenForHolder(holder, previous)).orElse(null);
	}

	/**
	 * Obtain group of multiple configs based on group ID. This is useful when you have multiple config files for your mod.
	 *
	 * @param group    Group ID, usually mod ID
	 * @param previous Previously open screen
	 * @return Either new config group screen or null when no config exists under the provided group
	 */
	@Environment(EnvType.CLIENT)
	public static Screen getConfigScreenByGroup(String group, Screen previous) {
		List<ConfigHolder<?>> list = ConfigHolder.getConfigsByGroup(group);
		if (list.isEmpty())
			return null;
		return getConfigScreenByGroup(list, group, previous);
	}

	@Environment(EnvType.CLIENT)
	public static Screen getConfigScreenForHolder(ConfigHolder<?> holder, Screen previous) {
		Map<String, ConfigValue<?>> valueMap = holder.getValueMap();
		return new ConfigScreen(holder.getConfigId(), holder.getConfigId(), valueMap, previous);
	}

	@Environment(EnvType.CLIENT)
	public static Screen getConfigScreenByGroup(List<ConfigHolder<?>> group, String groupId, Screen previous) {
		return new ConfigGroupScreen(previous, groupId, group);
	}
}
