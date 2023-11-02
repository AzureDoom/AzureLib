package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Arrays;

public class IntArrayValue extends ConfigValue<int[]> implements ArrayValue {

    private boolean fixedSize;
    private IntegerValue.Range range;

    public IntArrayValue(ValueData<int[]> valueData) {
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
        this.range = intRange != null ? IntegerValue.Range.newBoundedRange(intRange.min(), intRange.max()) : IntegerValue.Range.unboundedInt();
    }

    @Override
    protected int[] getCorrectedValue(int[] in) {
        if (this.fixedSize) {
            int[] defaultArray = this.valueData.getDefaultValue();
            if (in.length != defaultArray.length) {
                ConfigUtils.logArraySizeCorrectedMessage(this.getId(), Arrays.toString(in), Arrays.toString(defaultArray));
                in = defaultArray;
            }
        }
        if (this.range == null)
            return in;
        for (int i = 0; i < in.length; i++) {
            int value = in[i];
            if (!this.range.isWithin(value)) {
                int corrected = this.range.clamp(value);
                ConfigUtils.logCorrectedMessage(this.getId() + "[" + i + "]", value, corrected);
                in[i] = corrected;
            }
        }
        return in;
    }

    @Override
    public void serialize(IConfigFormat format) {
        format.writeIntArray(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readIntArray(this.getId()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int[] ints = this.get();
        for (int i = 0; i < ints.length; i++) {
            builder.append(this.elementToString(ints[i]));
            if (i < ints.length - 1) {
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
            int[] arr = (int[]) value.get();
            buffer.writeInt(arr.length);
            for (int v : arr) {
                buffer.writeInt(v);
            }
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            int[] arr = new int[buffer.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = buffer.readInt();
            }
            return arr;
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new IntArrayValue(ValueData.of(name, (int[]) value, context, comments));
        }
    }
}
