package mod.azure.azurelib.common.internal.common.config.value;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

public class StringValue extends ConfigValue<String> {

    private Pattern pattern;
    private String descriptor;

    public StringValue(ValueData<String> valueData) {
        super(valueData);
    }

    @Override
    protected void readFieldData(Field field) {
        Configurable.StringPattern stringPattern = field.getAnnotation(Configurable.StringPattern.class);
        if (stringPattern != null) {
            String value = stringPattern.value();
            this.descriptor = stringPattern.errorDescriptor().isEmpty() ? null : stringPattern.errorDescriptor();
            try {
                this.pattern = Pattern.compile(value, stringPattern.flags());
            } catch (IllegalArgumentException e) {
            	AzureLib.LOGGER.error(ConfigIO.MARKER, "Invalid @StringPattern value for {} field - {}", this.getId(), e);
            }
        }
    }

    @Override
    protected String getCorrectedValue(String in) {
        if (this.pattern != null && (!this.pattern.matcher(in).matches())) {
            String defaultValue = this.valueData.getDefaultValue();
            if (!this.pattern.matcher(defaultValue).matches()) {
                throw new IllegalArgumentException(String.format("Invalid config default value '%s' for field '%s' - does not match required pattern \\%s\\", defaultValue, this.getId(), this.pattern.toString()));
            }
            ConfigUtils.logCorrectedMessage(this.getId(), in, defaultValue);
            return defaultValue;
        }
        return in;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeString(this.getId(), this.get());
    }

    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        this.set(format.readString(this.getId()));
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getErrorDescriptor() {
        return descriptor;
    }

    public static final class Adapter extends TypeAdapter {

        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            buffer.writeUtf((String) value.get());
        }

        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            return buffer.readUtf();
        }

        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new StringValue(ValueData.of(name, (String) value, context, comments));
        }
    }
}
