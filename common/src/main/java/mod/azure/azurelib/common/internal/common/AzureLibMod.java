package mod.azure.azurelib.common.internal.common;

import mod.azure.azurelib.common.api.common.config.Config;
import mod.azure.azurelib.common.internal.common.config.AzureLibConfig;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.internal.common.config.ConfigHolderRegistry;
import mod.azure.azurelib.common.internal.common.config.format.ConfigFormats;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormatHandler;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;

public final class AzureLibMod {

    public static AzureLibConfig config;

    /**
     * Registers your config class. Config will be immediately loaded upon calling.
     *
     * @param configClass   Your config class
     * @param formatFactory File format to be used by this config class. You can use values from {@link ConfigFormats}
     *                      for example.
     * @param <C>           Config type
     * @return Config holder containing your config instance. You obtain it by calling
     *         {@link ConfigHolder#getConfigInstance()} method.
     */
    public static <C> ConfigHolder<C> registerConfig(Class<C> configClass, IConfigFormatHandler formatFactory) {
        Config config = configClass.getAnnotation(Config.class);
        if (config == null) {
            throw new IllegalArgumentException("Config class must be annotated with '@Config' annotation");
        }
        String id = config.id();
        String filename = config.filename();
        if (filename.isEmpty()) {
            filename = id;
        }
        String group = config.group();
        if (group.isEmpty()) {
            group = id;
        }
        ConfigHolder<C> holder = new ConfigHolder<>(configClass, id, filename, group, formatFactory);
        ConfigHolderRegistry.registerConfig(holder);
        if (configClass.getAnnotation(Config.NoAutoSync.class) == null) {
            ConfigIO.FILE_WATCH_MANAGER.addTrackedConfig(holder);
        }
        return holder;
    }

    private AzureLibMod() {
        throw new UnsupportedOperationException();
    }
}
