package mod.azure.azurelib.common.internal.common.config.value;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.regex.Pattern;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

public class StringArrayValue extends ConfigValue<String[]> implements ArrayValue {

    private boolean fixedSize;
    private Pattern pattern;
    private String defaultElementValue = "";

    public StringArrayValue(ValueData<String[]> valueData) {
        super(valueData);
    }

    @Override
    public boolean isFixedSize() {
        return fixedSize;
    }

    @Override
    protected void readFieldData(Field field) {
        this.fixedSize = field.getAnnotation(Configurable.FixedSize.class) != null;
        Configurable.StringPattern stringPattern = field.getAnnotation(Configurable.StringPattern.class);
        if (stringPattern != null) {
            String value = stringPattern.value();
            this.defaultElementValue = stringPattern.defaultValue();
            try {
                this.pattern = Pattern.compile(value, stringPattern.flags());
            } catch (IllegalArgumentException e) {
            	AzureLib.LOGGER.error(ConfigIO.MARKER, "Invalid @StringPattern value for {} field - {}", this.getId(), e);
            }
            if (this.pattern != null && !this.pattern.matcher(this.defaultElementValue).matches()) {
                throw new IllegalArgumentException(String.format("Invalid config default value '%s' for field '%s' - does not match required pattern \\%s\\", this.defaultElementValue, this.getId(), this.pattern.toString()));
            }
        }
    }

    @Override
    protected String[] getCorrectedValue(String[] in) {
        String[] defaultArray = this.valueData.getDefaultValue();
        if (this.fixedSize && (in.length != defaultArray.length)) {
            ConfigUtils.logArraySizeCorrectedMessage(this.getId(), Arrays.toString(in), Arrays.toString(defaultArray));
            return defaultArray;
        }
        if (this.pattern != null) {
            for (int i = 0; i < in.length; i++) {
                String string = in[i];
                if (!this.pattern.matcher(string).matches()) {
                    ConfigUtils.logCorrectedMessage(this.getId() + "[" + i + "]", string, this.defaultElementValue);
                    in[i] = this.defaultElementValue;
                }
            }
        }
        return in;
    }

    public String getDefaultElementValue() {
        return defaultElementValue;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeStringArray(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readStringArray(this.getId()));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        String[] strings = this.get();
        for (int i = 0; i < strings.length; i++) {
            builder.append(this.elementToString(strings[i]));
            if (i < strings.length - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            String[] arr = (String[]) value.get();
            buffer.writeInt(arr.length);
            for (String v : arr) {
                buffer.writeUtf(v);
            }
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            String[] arr = new String[buffer.readInt()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = buffer.readUtf();
            }
            return arr;
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new StringArrayValue(ValueData.of(name, (String[]) value, context, comments));
        }
    }
}
