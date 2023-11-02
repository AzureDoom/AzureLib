package mod.azure.azurelib.common.internal.common.config;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.client.config.IValidationHandler;
import mod.azure.azurelib.common.internal.common.AzureLibMod;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapters;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormatHandler;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.config.value.ObjectValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages config values and stores some default parameters of your config class.
 * This class also acts as config registry.
 *
 * @param <CFG> Your config type
 * @author Toma
 */
public final class ConfigHolder<CFG> {

    // Map of all registered configs
    private static final Map<String, ConfigHolder<?>> REGISTERED_CONFIGS = new HashMap<>();
    // Unique config ID
    private final String configId;
    // Config filename without extension
    private final String filename;
    // Config group, same as config ID unless changed
    private final String group;
    // Registered config instance
    private final CFG configInstance;
    // Type of config
    private final Class<CFG> configClass;
    // File format used by this config
    private final IConfigFormatHandler format;
    // Mapping of all config values
    private final Map<String, ConfigValue<?>> valueMap = new LinkedHashMap<>();
    // Map of fields which will be synced to client upon login
    private final Map<String, ConfigValue<?>> networkSerializedFields = new HashMap<>();
    // Set of file refresh listeners
    private final Set<IFileRefreshListener<CFG>> fileRefreshListeners = new HashSet<>();
    // Lock for async operations
    private final Object lock = new Object();

    public ConfigHolder(Class<CFG> cfgClass, String configId, String filename, String group, IConfigFormatHandler format) {
        this.configClass = cfgClass;
        this.configId = configId;
        this.filename = filename;
        this.group = group;
        try {
            this.configInstance = cfgClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException |
                 IllegalAccessException e) {
            AzureLib.LOGGER.fatal(AzureLib.MAIN_MARKER, "Failed to instantiate config class for {} config", configId);
            throw new AzureLibException("Config create failed", e);
        }
        try {
            serializeType(configClass, configInstance, true);
        } catch (IllegalAccessException e) {
            throw new AzureLibException("Config serialize failed", e);
        }
        this.format = format;
        this.loadNetworkFields(valueMap, networkSerializedFields);
    }

    /**
     * Registers config to internal registry. You should never call
     * this method. Instead, use {@link AzureLibMod#registerConfig(Class, IConfigFormatHandler)} for config registration
     *
     * @param holder Config holder to be registered
     */
    public static void registerConfig(ConfigHolder<?> holder) {
        REGISTERED_CONFIGS.put(holder.configId, holder);
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
        return value == null ? Optional.empty() : Optional.of(value);
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
                .filter(configHolder -> configHolder.group.equals(group))
                .collect(Collectors.toList());
    }

    /**
     * Obtain all configs which have some network serialized values
     *
     * @return Set of config holders which need to be synchronized to client
     */
    public static Set<String> getSynchronizedConfigs() {
        return REGISTERED_CONFIGS.entrySet()
                .stream()
                .filter(e -> e.getValue().networkSerializedFields.size() > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Register new file refresh listener for this config holder
     *
     * @param listener The file listener
     */
    public void addFileRefreshListener(IFileRefreshListener<CFG> listener) {
        this.fileRefreshListeners.add(Objects.requireNonNull(listener));
    }

    /**
     * @return ID of this config
     */
    public String getConfigId() {
        return configId;
    }

    /**
     * @return Filename without extension for this config
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return Group ID of this config
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return Your registered config
     */
    public CFG getConfigInstance() {
        return configInstance;
    }

    /**
     * @return Type of config
     */
    public Class<CFG> getConfigClass() {
        return configClass;
    }

    /**
     * @return File format factory for this config
     */
    public IConfigFormatHandler getFormat() {
        return format;
    }

    /**
     * @return Collection of mapped config values
     */
    public Collection<ConfigValue<?>> values() {
        return this.valueMap.values();
    }

    /**
     * @return Map ID-ConfigValue for this config
     */
    public Map<String, ConfigValue<?>> getValueMap() {
        return valueMap;
    }

    /**
     * @return Map ID-ConfigValue for network serialization
     */
    public Map<String, ConfigValue<?>> getNetworkSerializedFields() {
        return networkSerializedFields;
    }

    /**
     * Dispatches file refresh event to all registered listeners
     */
    public void dispatchFileRefreshEvent() {
        this.fileRefreshListeners.forEach(listener -> listener.onFileRefresh(this));
    }

    /**
     * @return Lock for async operations. Used for IO operations currently
     */
    public Object getLock() {
        return lock;
    }

    private Map<String, ConfigValue<?>> serializeType(Class<?> type, Object instance, boolean saveValue) throws IllegalAccessException {
        Map<String, ConfigValue<?>> map = new LinkedHashMap<>();
        Field[] fields = type.getFields();
        for (Field field : fields) {
            Configurable value = field.getAnnotation(Configurable.class);
            if (value == null)
                continue;
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                AzureLib.LOGGER.warn(ConfigIO.MARKER, "Skipping config field {}, only instance non-final types are supported", field);
                continue;
            }
            TypeAdapter adapter = TypeAdapters.forType(field.getType());
            if (adapter == null) {
                AzureLib.LOGGER.warn(ConfigIO.MARKER, "Missing adapter for type {}, skipping serialization", field.getType());
                continue;
            }
            String[] comments = new String[0];
            Configurable.Comment comment = field.getAnnotation(Configurable.Comment.class);
            if (comment != null) {
                comments = comment.value();
            }
            field.setAccessible(true);
            ConfigValue<?> cfgValue = adapter.serialize(field.getName(), comments, field.get(instance), (type1, instance1) -> serializeType(type1, instance1, false), new TypeAdapter.AdapterContext() {
                @Override
                public TypeAdapter getAdapter() {
                    return adapter;
                }

                @Override
                public Field getOwner() {
                    return field;
                }

                @Override
                public void setFieldValue(Object value) {
                    field.setAccessible(true);
                    try {
                        adapter.setFieldValue(field, instance, value);
                    } catch (IllegalAccessException e) {
                        AzureLib.LOGGER.error(ConfigIO.MARKER, "Failed to update config value for field {} from {} to a new value {} due to error {}", field.getName(), type, value, e);
                    }
                }
            });
            Configurable.ValueUpdateCallback callback = field.getAnnotation(Configurable.ValueUpdateCallback.class);
            if (callback != null) {
                this.processCallback(callback, type, instance, cfgValue);
            }
            cfgValue.processFieldData(field);
            map.put(field.getName(), cfgValue);
            if (saveValue) {
                this.assignValue(cfgValue);
            }
        }
        return map;
    }

    private <T> void processCallback(Configurable.ValueUpdateCallback callback, Class<?> type, Object instance, ConfigValue<T> value) {
        String methodName = callback.method();
        try {
            Class<?> valueType = value.getValueType();
            if (callback.allowPrimitivesMapping()) {
                valueType = ConfigUtils.remapPrimitiveType(valueType);
            }
            Method method = type.getDeclaredMethod(methodName, valueType, IValidationHandler.class);
            ConfigValue.SetValueCallback<T> setValueCallback = (val, handler) -> {
                try {
                    method.setAccessible(true);
                    method.invoke(instance, val, handler);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    AzureLib.LOGGER.error(ConfigIO.MARKER, "Error occurred while invoking {} method: {}", method, e);
                }
            };
            value.setValueValidator(setValueCallback);
            AzureLib.LOGGER.debug(ConfigIO.MARKER, "Attached new value listener method '{}' for config value {}", methodName, value.getId());
        } catch (NoSuchMethodException e) {
            AzureLib.LOGGER.error(ConfigIO.MARKER, "Unable to map method {} for config value {} due to {}", methodName, value.getId(), e);
        } catch (Exception e) {
            AzureLib.LOGGER.fatal(ConfigIO.MARKER, "Fatal error occurred while trying to map value listener for {} method", methodName);
            throw new AzureLibException("Value listener map failed", e);
        }
    }

    private <T> void assignValue(ConfigValue<T> value) {
        this.valueMap.put(value.getId(), value);
    }

    private void loadNetworkFields(Map<String, ConfigValue<?>> src, Map<String, ConfigValue<?>> dest) {
        src.values().forEach(value -> {
            if (value instanceof ObjectValue objValue) {
                Map<String, ConfigValue<?>> data = objValue.get();
                loadNetworkFields(data, dest);
            } else {
                if (!value.shouldSynchronize())
                    return;
                String path = value.getFieldPath();
                dest.put(path, value);
            }
        });
    }

    /**
     * Listener which is triggered when config file changes on disk
     *
     * @param <CFG> Config type
     * @author Toma
     */
    @FunctionalInterface
    public interface IFileRefreshListener<CFG> {
        void onFileRefresh(ConfigHolder<CFG> holder);
    }
}
