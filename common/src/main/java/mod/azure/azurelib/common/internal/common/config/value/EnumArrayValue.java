package mod.azure.azurelib.common.internal.common.config.value;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import mod.azure.azurelib.common.internal.common.config.Configurable;
import mod.azure.azurelib.common.internal.common.config.format.IConfigFormat;
import mod.azure.azurelib.common.internal.common.config.adapter.TypeAdapter;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import net.minecraft.network.FriendlyByteBuf;

public class EnumArrayValue<E extends Enum<E>> extends ConfigValue<E[]> implements ArrayValue {

    private boolean fixedSize;

    public EnumArrayValue(ValueData<E[]> value) {
        super(value);
    }

    @Override
    public boolean isFixedSize() {
        return fixedSize;
    }

    @Override
    protected void serialize(IConfigFormat format) {
        format.writeEnumArray(getId(), get());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void deserialize(IConfigFormat format) throws ConfigValueMissingException {
        Class<E> type = (Class<E>) getValueType().getComponentType();
        set(format.readEnumArray(getId(), type));
    }

    @Override
    protected void readFieldData(Field field) {
        this.fixedSize = field.getAnnotation(Configurable.FixedSize.class) != null;
    }

    public static final class Adapter<E extends Enum<E>> extends TypeAdapter {

        @SuppressWarnings("unchecked")
        @Override
        public ConfigValue<?> serialize(String name, String[] comments, Object value, TypeSerializer serializer, AdapterContext context) throws IllegalAccessException {
            return new EnumArrayValue<>(ValueData.of(name, (E[]) value, context, comments));
        }

        @SuppressWarnings("unchecked")
        @Override
        public void encodeToBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            E[] values = (E[]) value.get();
            buffer.writeInt(values.length);
            for (E e : values) {
                buffer.writeEnum(e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object decodeFromBuffer(ConfigValue<?> value, FriendlyByteBuf buffer) {
            int count = buffer.readInt();
            Class<E> type = (Class<E>) value.getValueType().getComponentType();
            E[] enumArray = (E[]) Array.newInstance(type, count);
            for (int i = 0; i < count; i++) {
                enumArray[i] = buffer.readEnum(type);
            }
            return enumArray;
        }
    }
}
