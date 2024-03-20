package mod.azure.azurelib.common.internal.common.config;

import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormatHandler;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Boston Vanseghi
 */
public final class ConfigHolderRegistry {

    // Map of all registered configs
    private static final Map<String, ConfigHolder<?>> REGISTERED_CONFIGS = new HashMap<>();

    /**
     * Registers config to internal registry. You should never call
     * this method. Instead, use {@link AzureLibMod#registerConfig(Class, IConfigFormatHandler)} for config registration
     *
     * @param holder Config holder to be registered
     */
    public static void registerConfig(ConfigHolder<?> holder) {
        REGISTERED_CONFIGS.put(holder.getConfigId(), holder);
        ConfigIO.processConfig(holder);
    }

    /**
     * Allows you to get your config holder based on ID
     *
     * @param id    Config ID
     * @param <CFG> Config type
     * @return Optional with config holder when such object exists
     */
    public static <CFG> Optional<ConfigHolder<CFG>> getConfig(String id) {
        ConfigHolder<CFG> value = (ConfigHolder<CFG>) REGISTERED_CONFIGS.get(id);
        return Optional.ofNullable(value);
    }

    /**
     * Groups all configs from registry into Group-List
     *
     * @return Mapped values
     */
    public static Map<String, List<ConfigHolder<?>>> getConfigGroupingByGroup() {
        return REGISTERED_CONFIGS.values().stream().collect(Collectors.groupingBy(ConfigHolder::getGroup));
    }

    /**
     * Returns list of config holders for the specified group
     *
     * @param group Group ID
     * @return List with config holders. May be empty.
     */
    public static List<ConfigHolder<?>> getConfigsByGroup(String group) {
        return REGISTERED_CONFIGS.values().stream()
            .filter(configHolder -> configHolder.getGroup().equals(group))
            .toList();
    }

    /**
     * Obtain all configs which have some network serialized values
     *
     * @return Set of config holders which need to be synchronized to client
     */
    public static Set<String> getSynchronizedConfigs() {
        return REGISTERED_CONFIGS.entrySet()
            .stream()
            .filter(e -> !e.getValue().getNetworkSerializedFields().isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }

    private ConfigHolderRegistry() {
        throw new UnsupportedOperationException();
    }
}
