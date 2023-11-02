package mod.azure.azurelib.common.internal.common.config.value;

import java.util.Objects;

import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import org.jetbrains.annotations.Nullable;

public final class ValueData<T> implements IDescriptionProvider {

    private final String id;
    private final String[] tooltip;
    private final T defaultValue;
    private final TypeAdapter.AdapterContext context;
    private final Class<T> valueType;
    @Nullable
    private ConfigValue<?> parent;

    private ValueData(String id, String[] tooltip, T defaultValue, TypeAdapter.AdapterContext context) {
        this.id = id;
        this.tooltip = tooltip;
        this.defaultValue = defaultValue;
        this.context = context;
        this.valueType = (Class<T>) defaultValue.getClass();
    }

    public static <V> ValueData<V> of(String id, V value, TypeAdapter.AdapterContext setter, String... comments) {
        return new ValueData<>(id, comments, Objects.requireNonNull(value), setter);
    }

    public String getId() {
        return id;
    }

    @Override
    public String[] getDescription() {
        return tooltip;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setValueToMemory(Object value) {
        this.context.setFieldValue(value);
    }

    public void setParent(@Nullable ConfigValue<?> parent) {
        this.parent = parent;
    }

    @Nullable
    public ConfigValue<?> getParent() {
        return parent;
    }

    public TypeAdapter.AdapterContext getContext() {
        return context;
    }

    public Class<T> getValueType() {
        return valueType;
    }
}
