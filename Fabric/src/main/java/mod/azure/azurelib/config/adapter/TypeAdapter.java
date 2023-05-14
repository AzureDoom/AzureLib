package mod.azure.azurelib.config.adapter;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Map;

import mod.azure.azurelib.config.value.ConfigValue;

public abstract class TypeAdapter {

    public abstract ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException;

    public abstract void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer);

    public abstract Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer);

    public void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
        field.set(instance, value);
    }

    @FunctionalInterface
    public interface TypeSerializer {
        Map<String, ConfigValue<?>> serialize(Class<?> type, Object instance) throws IllegalAccessException;
    }

    public interface AdapterContext {

        TypeAdapter getAdapter();

        Field getOwner();

        void setFieldValue(Object value);
    }
}
