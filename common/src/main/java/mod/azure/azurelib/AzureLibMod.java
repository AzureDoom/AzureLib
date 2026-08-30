/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib;

import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.ConfigHolderRegistry;
import mod.azure.azurelib.config.format.ConfigFormats;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.io.ConfigIO;

public final class AzureLibMod {

    private AzureLibMod() {
        throw new UnsupportedOperationException();
    }

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
        var config = configClass.getAnnotation(Config.class);
        if (config == null) {
            throw new IllegalArgumentException("Config class must be annotated with '@Config' annotation");
        }
        var id = config.id();
        var filename = config.filename();
        if (filename.isEmpty()) {
            filename = id;
        }
        var group = config.group();
        if (group.isEmpty()) {
            group = id;
        }
        var holder = new ConfigHolder<>(configClass, id, filename, group, formatFactory);
        ConfigHolderRegistry.registerConfig(holder);
        if (configClass.getAnnotation(Config.NoAutoSync.class) == null) {
            ConfigIO.FILE_WATCH_MANAGER.addTrackedConfig(holder);
        }
        return holder;
    }
}
