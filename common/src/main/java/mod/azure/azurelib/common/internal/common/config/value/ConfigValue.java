package mod.azure.azurelib.common.internal.common.config.value;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import mod.azure.azurelib.common.internal.client.config.IValidationHandler;
import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;

public abstract class ConfigValue<T> implements Supplier<T>{

    protected final ValueData<T> valueData;
    private T value;
    private boolean synchronizeToClient;
    @Nullable
    private SetValueCallback<T> setValueCallback;

    protected ConfigValue(ValueData<T> valueData) {
        this.valueData = valueData;
        this.useDefaultValue();
    }

    @Override
    public final T get() {
        return value;
    }

    public final boolean shouldSynchronize() {
        return synchronizeToClient;
    }

    public final void set(T value) {
        T corrected = this.getCorrectedValue(value);
        if (corrected == null) {
            this.useDefaultValue();
            corrected = this.get();
        }
        this.value = corrected;
        this.valueData.setValueToMemory(corrected);
    }

    public final void setWithValidationHandler(T value, IValidationHandler handler) {
        this.invokeValueValidator(value, handler);
        this.set(value);
    }

    public final String getId() {
        return this.valueData.getId();
    }

    public final void setParent(@Nullable ConfigValue<?> parent) {
        this.valueData.setParent(parent);
    }

    public final void processFieldData(Field field) {
        this.synchronizeToClient = field.getAnnotation(Configurable.Synchronized.class) != null;
        this.readFieldData(field);
    }

    protected void readFieldData(Field field) {

    }

    protected T getCorrectedValue(T in) {
        return in;
    }

    public final void useDefaultValue() {
        this.set(this.valueData.getDefaultValue());
    }

    public void setValueValidator(SetValueCallback<T> callback) {
        this.setValueCallback = callback;
    }

    public final void invokeValueValidator(T value, IValidationHandler handler) {
        if (this.setValueCallback != null) {
            this.setValueCallback.processValue(value, handler);
        }
    }

    protected abstract void serialize(IConfigFormat format);

    public final void serializeValue(IConfigFormat format) {
        format.addComments(valueData);
        this.serialize(format);
    }

    public final String[] getDescription() {
        return this.valueData.getDescription();
    }

    protected abstract void deserialize(IConfigFormat format) throws ConfigValueMissingException;

    public final void deserializeValue(IConfigFormat format) {
        try {
            this.deserialize(format);
        } catch (ConfigValueMissingException e) {
            this.useDefaultValue();
            ConfigUtils.logCorrectedMessage(this.getId(), null, this.get());
        }
    }

    public final TypeAdapter.AdapterContext getSerializationContext() {
        return this.valueData.getContext();
    }

    public final TypeAdapter getAdapter() {
        return this.getSerializationContext().getAdapter();
    }

    public final Class<T> getValueType() {
        return this.valueData.getValueType();
    }

    public final String getFieldPath() {
        List<String> paths = new ArrayList<>();
        paths.add(this.getId());
        ConfigValue<?> parent = this;
        while ((parent = parent.valueData.getParent()) != null) {
            paths.add(parent.getId());
        }
        Collections.reverse(paths);
        return paths.stream().reduce("$", (a, b) -> a + "." + b);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @FunctionalInterface
    public interface SetValueCallback<V> {

        void processValue(V value, IValidationHandler handler);
    }
}
