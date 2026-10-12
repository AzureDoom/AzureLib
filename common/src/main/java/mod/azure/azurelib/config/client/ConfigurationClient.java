/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.ConfigHolderRegistry;
import mod.azure.azurelib.config.client.screen.ConfigGroupScreen;
import mod.azure.azurelib.config.client.screen.ConfigScreen;

/**
 * Client API entry point for AzureLib configs. Use this to obtain config screens for your mod-list integration.
 */
public final class ConfigurationClient {

    private ConfigurationClient() {
        throw new UnsupportedOperationException();
    }

    /**
     * @param configClass Your config class (annotated with {@link Config})
     * @param previous    Previously open screen
     * @return New config screen, or {@code null} when no config is registered for the class
     */
    @Nullable
    public static Screen getConfigScreen(Class<?> configClass, Screen previous) {
        Config cfg = configClass.getAnnotation(Config.class);
        if (cfg == null) {
            return null;
        }
        return getConfigScreen(cfg.id(), previous);
    }

    /**
     * @param configId ID of your config
     * @param previous Previously open screen
     * @return New config screen, or {@code null} when no config exists with the provided ID
     */
    @Nullable
    public static Screen getConfigScreen(String configId, Screen previous) {
        return ConfigHolderRegistry.getConfig(configId)
            .map(holder -> (Screen) new ConfigScreen(holder, getTitle(holder), holder.getValueMap(), previous))
            .orElse(null);
    }

    /**
     * Obtains a selection screen for every config registered under the given group (usually your mod ID).
     *
     * @return Group screen, or {@code null} when nothing is registered under the group
     */
    @Nullable
    public static Screen getConfigScreenByGroup(String group, Screen previous) {
        List<ConfigHolder<?>> list = ConfigHolderRegistry.getConfigsByGroup(group);
        if (list.isEmpty())
            return null;
        return getConfigScreenByGroup(list, group, previous);
    }

    public static Screen getConfigScreenByGroup(List<ConfigHolder<?>> group, String groupId, Screen previous) {
        return new ConfigGroupScreen(previous, groupId, group);
    }

    /**
     * Convenience for mod-list integrations: opens the config directly when the group has a single config, otherwise
     * the group selection screen.
     */
    @Nullable
    public static Screen getScreenForGroup(List<ConfigHolder<?>> group, String groupId, Screen previous) {
        if (group.isEmpty())
            return null;
        if (group.size() == 1)
            return getConfigScreen(group.getFirst().getConfigId(), previous);
        return getConfigScreenByGroup(group, groupId, previous);
    }

    public static Component getTitle(ConfigHolder<?> holder) {
        return Component.translatable("config.screen." + holder.getConfigId());
    }
}
