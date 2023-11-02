package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Map;

public class ObjectValue extends ConfigValue<Map<String, ConfigValue<?>>> {

    public ObjectValue(ValueData<Map<String, ConfigValue<?>>> valueData) {
        super(valueData);
        this.get().values().forEach(value -> value.setParent(this));
    }

    @Override
    public void serialize(IConfigFormat format) {
        format.writeMap(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        format.readMap(this.getId(), this.get().values());
    }

    @Override
    public void setValueValidator(SetValueCallback<Map<String, ConfigValue<?>>> callback) {
        throw new UnsupportedOperationException("Cannot attach value validator to Object types!");
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            Class<?> type = value.getClass();
            Map<String, ConfigValue<?>> map = serializer.serialize(type, value);
            return new ObjectValue(ValueData.of(name, map, context, comments));
        }

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            return null;
        }

        @Override
        public void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
            // Do not set anything, keep existing instance
        }
    }
}
