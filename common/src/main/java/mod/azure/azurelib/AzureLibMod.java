package mod.azure.azurelib;

import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.TestingConfig;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.io.ConfigIO;
import mod.azure.azurelib.entities.TickingLightBlock;
import mod.azure.azurelib.entities.TickingLightEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class AzureLibMod {

	public static BlockEntityType<TickingLightEntity> TICKING_LIGHT_ENTITY;
	public static final TickingLightBlock TICKING_LIGHT_BLOCK = new TickingLightBlock();
	public static TestingConfig config;

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
