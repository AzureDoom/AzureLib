package mod.azure.azurelib.config;

import java.util.*;
import java.util.stream.Collectors;

import mod.azure.azurelib.AzureLibMod;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.io.ConfigIO;

/**
 * @author Boston Vanseghi
 */
public final class ConfigHolderRegistry {

    public static final Map<String, ConfigHolder<?>> REGISTERED_CONFIGS = new HashMap<>();

    private ConfigHolderRegistry() {
        throw new UnsupportedOperationException();
    }

    /**
     * Registers config to internal registry. You should never call this method. Instead, use
     * {@link AzureLibMod#registerConfig(Class, IConfigFormatHandler)} for config registration
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
     * @param id  Config ID
     * @param <C> Config type
     * @return Optional with config holder when such object exists
     */
    public static <C> Optional<ConfigHolder<C>> getConfig(String id) {
        return Optional.ofNullable((ConfigHolder<C>) REGISTERED_CONFIGS.get(id));
    }

    public static Map<String, List<ConfigHolder<?>>> getConfigGroupingByGroup() {
        return REGISTERED_CONFIGS.values().stream().collect(Collectors.groupingBy(ConfigHolder::getGroup));
    }

    public static List<ConfigHolder<?>> getConfigsByGroup(String group) {
        return REGISTERED_CONFIGS.values()
            .stream()
            .filter(configHolder -> configHolder.getGroup().equals(group))
            .toList();
    }

    public static Set<String> getSynchronizedConfigs() {
        return REGISTERED_CONFIGS.entrySet()
            .stream()
            .filter(e -> !e.getValue().getNetworkSerializedFields().isEmpty())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
    }
}
