package mod.azure.azurelib.common.internal.client;

import mod.azure.azurelib.common.internal.client.config.screen.ConfigGroupScreen;
import mod.azure.azurelib.common.internal.client.config.screen.ConfigScreen;
import mod.azure.azurelib.common.internal.common.config.ConfigHolder;
import mod.azure.azurelib.common.api.common.config.Config;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class AzureLibClient {

    /**
     * You can obtain default config screen based on provided config class.
     *
     * @param configClass Your config class
     * @param previous    Previously open screen
     * @return Either new config screen or {@code null} when no config exists for the provided class
     */
    @Nullable
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
    public static Screen getConfigScreenByGroup(String group, Screen previous) {
        List<ConfigHolder<?>> list = ConfigHolder.getConfigsByGroup(group);
        if (list.isEmpty())
            return null;
        return getConfigScreenByGroup(list, group, previous);
    }

    public static Screen getConfigScreenForHolder(ConfigHolder<?> holder, Screen previous) {
        Map<String, ConfigValue<?>> valueMap = holder.getValueMap();
        return new ConfigScreen(holder.getConfigId(), holder.getConfigId(), valueMap, previous);
    }

    public static Screen getConfigScreenByGroup(List<ConfigHolder<?>> group, String groupId, Screen previous) {
        return new ConfigGroupScreen(previous, groupId, group);
    }
}
