package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public final class BooleanValue extends ConfigValue<Boolean> {

    public BooleanValue(ValueData<Boolean> valueData) {
        super(valueData);
    }

    @Override
    public void serialize(IConfigFormat format) {
        boolean value = this.get();
        format.writeBoolean(this.getId(), value);
    }

    @Override
    public void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        String field = this.getId();
        this.set(format.readBoolean(field));
    }

    public static class Adapter extends TypeAdapter {

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) {
            return new BooleanValue(ValueData.of(name, (boolean) value, context, comments));
        }

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            buffer.writeBoolean((Boolean) value.get());
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            return buffer.readBoolean();
        }

        @Override
        public void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
            field.setBoolean(instance, (boolean) value);
        }
    }
}
