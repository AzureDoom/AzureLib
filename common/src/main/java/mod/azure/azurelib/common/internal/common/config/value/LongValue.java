package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public class LongValue extends IntegerValue<Long> {

    public LongValue(ValueData<Long> valueData) {
        super(valueData, Range.unboundedLong());
    }

    @Override
    public Long getCorrectedValue(Long in) {
        if (this.range == null)
            return in;
        if (!this.range.isWithin(in)) {
            long corrected = this.range.clamp(in);
            ConfigUtils.logCorrectedMessage(this.getId(), in, corrected);
            return corrected;
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeLong(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readLong(this.getId()));
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new LongValue(ValueData.of(name, (long) value, context, comments));
        }

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            buffer.writeLong((Long) value.get());
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            return buffer.readLong();
        }

        @Override
        public void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
            field.setLong(instance, (Long) value);
        }
    }
}
