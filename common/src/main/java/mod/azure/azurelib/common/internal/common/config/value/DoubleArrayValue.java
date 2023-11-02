package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Arrays;

public class DoubleArrayValue extends ConfigValue<double[]> implements ArrayValue {

    private boolean fixedSize;
    private DecimalValue.Range range;

    public DoubleArrayValue(ValueData<double[]> valueData) {
        super(valueData);
    }

    @Override
    public boolean isFixedSize() {
        return fixedSize;
    }

    @Override
    protected void readFieldData(Field field) {
        this.fixedSize = field.getAnnotation(Configurable.FixedSize.class) != null;
        Configurable.DecimalRange decimalRange = field.getAnnotation(Configurable.DecimalRange.class);
        this.range = decimalRange != null ? DecimalValue.Range.newBoundedRange(decimalRange.min(), decimalRange.max()) : DecimalValue.Range.unboundedDouble();
    }

    @Override
    protected double[] getCorrectedValue(double[] in) {
        if (this.fixedSize) {
            double[] defaultArray = this.valueData.getDefaultValue();
            if (in.length != defaultArray.length) {
                ConfigUtils.logArraySizeCorrectedMessage(this.getId(), Arrays.toString(in), Arrays.toString(defaultArray));
                in = defaultArray;
            }
        }
        if (this.range == null)
            return in;
        for (int i = 0; i < in.length; i++) {
            double value = in[i];
            if (!this.range.isWithin(value)) {
                double corrected = this.range.clamp(value);
                ConfigUtils.logCorrectedMessage(this.getId() + "[" + i + "]", value, corrected);
                in[i] = corrected;
            }
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeDoubleArray(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readDoubleArray(this.getId()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        double[] doubles = this.get();
        for (int i = 0; i < doubles.length; i++) {
            builder.append(this.elementToString(doubles[i]));
            if (i < doubles.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public DecimalValue.Range getRange() {
        return range;
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            double[] arr = (double[]) value.get();
            buffer.writeInt(arr.length);
            for (double v : arr) {
                buffer.writeDouble(v);
            }
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            double[] arr = new double[buffer.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = buffer.readDouble();
            }
            return arr;
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new DoubleArrayValue(ValueData.of(name, (double[]) value, context, comments));
        }
    }
}
