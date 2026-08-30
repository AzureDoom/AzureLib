/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.adapter.TypeAdapter;
import mod.azure.azurelib.config.adapter.TypeAdapters;
import mod.azure.azurelib.config.client.IValidationHandler;
import mod.azure.azurelib.config.format.IConfigFormatHandler;
import mod.azure.azurelib.config.value.ConfigValue;
import mod.azure.azurelib.config.value.ObjectValue;
import mod.azure.azurelib.util.AzureLibException;

import static mod.azure.azurelib.config.ConfigHolderRegistry.REGISTERED_CONFIGS;

/**
 * Manages config values and stores some default parameters of your config class. This class also acts as config
 * registry.
 *
 * @param <C> Your config type
 * @author Toma
 */
public final class ConfigHolder<C> {

    private final String configId;

    private final String filename;

    private final String group;

    private final C configInstance;

    private final Class<C> configClass;

    private final IConfigFormatHandler format;

    private final Map<String, ConfigValue<?>> valueMap = new LinkedHashMap<>();

    private final Map<String, ConfigValue<?>> networkSerializedFields = new HashMap<>();

    private final Set<IFileRefreshListener<C>> fileRefreshListeners = new HashSet<>();

    private final Object lock = new Object();

    public ConfigHolder(
        Class<C> cfgClass,
        String configId,
        String filename,
        String group,
        IConfigFormatHandler format
    ) {
        this.configClass = cfgClass;
        this.configId = configId;
        this.filename = filename;
        this.group = group;
        try {
            this.configInstance = cfgClass.getDeclaredConstructor().newInstance();
        } catch (
            NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e
        ) {
            AzureLib.LOGGER.error("Failed to instantiate config class for {} config", configId);
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

    public void addFileRefreshListener(IFileRefreshListener<C> listener) {
        this.fileRefreshListeners.add(Objects.requireNonNull(listener));
    }

    public String getConfigId() {
        return configId;
    }

    public String getFilename() {
        return filename;
    }

    public String getGroup() {
        return group;
    }

    public C getConfigInstance() {
        return configInstance;
    }

    public Class<C> getConfigClass() {
        return configClass;
    }

    public IConfigFormatHandler getFormat() {
        return format;
    }

    public Collection<ConfigValue<?>> values() {
        return this.valueMap.values();
    }

    public Map<String, ConfigValue<?>> getValueMap() {
        return valueMap;
    }

    public Map<String, ConfigValue<?>> getNetworkSerializedFields() {
        return networkSerializedFields;
    }

    public void dispatchFileRefreshEvent() {
        this.fileRefreshListeners.forEach(listener -> listener.onFileRefresh(this));
    }

    public Object getLock() {
        return lock;
    }

    private Map<String, ConfigValue<?>> serializeType(
        Class<?> type,
        Object instance,
        boolean saveValue
    ) throws IllegalAccessException {
        Map<String, ConfigValue<?>> map = new LinkedHashMap<>();
        Field[] fields = type.getFields();
        for (Field field : fields) {
            Configurable value = field.getAnnotation(Configurable.class);
            if (value == null)
                continue;
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
                AzureLib.LOGGER.warn(
                    "Skipping config field {}, only instance non-final types are supported",
                    field
                );
                continue;
            }
            TypeAdapter adapter = TypeAdapters.forType(field.getType());
            if (adapter == null) {
                AzureLib.LOGGER.warn(
                    "Missing adapter for type {}, skipping serialization",
                    field.getType()
                );
                continue;
            }
            String[] comments = new String[0];
            Configurable.Comment comment = field.getAnnotation(Configurable.Comment.class);
            if (comment != null) {
                comments = comment.value();
            }
            field.setAccessible(true);
            ConfigValue<?> cfgValue = adapter.serialize(
                field.getName(),
                comments,
                field.get(instance),
                (type1, instance1) -> serializeType(type1, instance1, false),
                new TypeAdapter.AdapterContext() {

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
                            AzureLib.LOGGER.error(
                                "Failed to update config value for field {} from {} to a new value {} due to error {}",
                                field.getName(),
                                type,
                                value,
                                e
                            );
                        }
                    }
                }
            );
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

    private <T> void processCallback(
        Configurable.ValueUpdateCallback callback,
        Class<?> type,
        Object instance,
        ConfigValue<T> value
    ) {
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
                    AzureLib.LOGGER.error("Error occurred while invoking {} method: {}", method, e);
                }
            };
            value.setValueValidator(setValueCallback);
            AzureLib.LOGGER.debug(
                "Attached new value listener method '{}' for config value {}",
                methodName,
                value.getId()
            );
        } catch (NoSuchMethodException e) {
            AzureLib.LOGGER.error(
                "Unable to map method {} for config value {} due to {}",
                methodName,
                value.getId(),
                e
            );
        } catch (Exception e) {
            AzureLib.LOGGER.error(
                "Fatal error occurred while trying to map value listener for {} method",
                methodName
            );
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

    public static Map<String, List<ConfigHolder<?>>> getConfigGroupingByGroup() {
        return REGISTERED_CONFIGS.values().stream().collect(Collectors.groupingBy(ConfigHolder::getGroup));
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
