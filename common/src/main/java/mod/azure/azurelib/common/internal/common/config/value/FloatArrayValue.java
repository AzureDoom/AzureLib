package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.Arrays;

public class FloatArrayValue extends ConfigValue<float[]> implements ArrayValue {

    private boolean fixedSize;
    private DecimalValue.Range range;

    public FloatArrayValue(ValueData<float[]> valueData) {
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
        this.range = decimalRange != null ? DecimalValue.Range.newBoundedRange(decimalRange.min(), decimalRange.max()) : DecimalValue.Range.unboundedFloat();
    }

    @Override
    protected float[] getCorrectedValue(float[] in) {
        if (this.fixedSize) {
            float[] defaultArray = this.valueData.getDefaultValue();
            if (in.length != defaultArray.length) {
                ConfigUtils.logArraySizeCorrectedMessage(this.getId(), Arrays.toString(in), Arrays.toString(defaultArray));
                in = defaultArray;
            }
        }
        if (this.range == null)
            return in;
        for (int i = 0; i < in.length; i++) {
            float value = in[i];
            if (!this.range.isWithin(value)) {
                float corrected = this.range.clamp(value);
                ConfigUtils.logCorrectedMessage(this.getId() + "[" + i + "]", value, corrected);
                in[i] = corrected;
            }
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeFloatArray(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readFloatArray(this.getId()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        float[] floats = this.get();
        for (int i = 0; i < floats.length; i++) {
            builder.append(this.elementToString(floats[i]));
            if (i < floats.length - 1) {
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
            float[] arr = (float[]) value.get();
            buffer.writeInt(arr.length);
            for (float v : arr) {
                buffer.writeFloat(v);
            }
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            float[] arr = new float[buffer.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = buffer.readFloat();
            }
            return arr;
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new FloatArrayValue(ValueData.of(name, (float[]) value, context, comments));
        }
    }
}
