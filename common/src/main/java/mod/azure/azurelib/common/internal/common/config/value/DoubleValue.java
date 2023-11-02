package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;

public class DoubleValue extends DecimalValue<Double> {

    public DoubleValue(ValueData<Double> valueData) {
        super(valueData, Range.unboundedDouble());
    }

    @Override
    public Double getCorrectedValue(Double in) {
        if (this.range == null)
            return in;
        if (!this.range.isWithin(in)) {
            double corrected = this.range.clamp(in);
            ConfigUtils.logCorrectedMessage(this.getId(), in, corrected);
            return corrected;
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeDouble(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readDouble(this.getId()));
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new DoubleValue(ValueData.of(name, (double) value, context, comments));
        }

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            buffer.writeDouble((Double) value.get());
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            return buffer.readDouble();
        }

        @Override
        public void setFieldValue(Field field, Object instance, Object value) throws IllegalAccessException {
            field.setDouble(instance, (Double) value);
        }
    }
}
