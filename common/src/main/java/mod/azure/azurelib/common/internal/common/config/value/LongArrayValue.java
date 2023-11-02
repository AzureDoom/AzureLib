package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Arrays;

public class LongArrayValue extends ConfigValue<long[]> implements ArrayValue {

    private boolean fixedSize;
    private IntegerValue.Range range;

    public LongArrayValue(ValueData<long[]> valueData) {
        super(valueData);
    }

    @Override
    public boolean isFixedSize() {
        return fixedSize;
    }

    @Override
    protected void readFieldData(Field field) {
        this.fixedSize = field.getAnnotation(Configurable.FixedSize.class) != null;
        Configurable.Range intRange = field.getAnnotation(Configurable.Range.class);
        this.range = intRange != null ? IntegerValue.Range.newBoundedRange(intRange.min(), intRange.max()) : IntegerValue.Range.unboundedLong();
    }

    @Override
    protected long[] getCorrectedValue(long[] in) {
        if (this.fixedSize) {
            long[] defaultArray = this.valueData.getDefaultValue();
            if (in.length != defaultArray.length) {
                ConfigUtils.logArraySizeCorrectedMessage(this.getId(), Arrays.toString(in), Arrays.toString(defaultArray));
                in = defaultArray;
            }
        }
        if (this.range == null)
            return in;
        for (int i = 0; i < in.length; i++) {
            long value = in[i];
            if (!this.range.isWithin(value)) {
                long corrected = this.range.clamp(value);
                ConfigUtils.logCorrectedMessage(this.getId() + "[" + i + "]", value, corrected);
                in[i] = corrected;
            }
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeLongArray(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readLongArray(this.getId()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        long[] longs = this.get();
        for (int i = 0; i < longs.length; i++) {
            builder.append(this.elementToString(longs[i]));
            if (i < longs.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public IntegerValue.Range getRange() {
        return range;
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            long[] arr = (long[]) value.get();
            buffer.writeInt(arr.length);
            for (long v : arr) {
                buffer.writeLong(v);
            }
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            long[] arr = new long[buffer.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = buffer.readLong();
            }
            return arr;
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new LongArrayValue(ValueData.of(name, (long[]) value, context, comments));
        }
    }
}
