package mod.azure.azurelib.config.adapter;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.config.value.*;

public final class TypeAdapters {

    private static final Map<TypeMatcher, TypeAdapter> ADAPTER_MAP = new HashMap<>();

    public static TypeAdapter forType(Class<?> type) {
        return ADAPTER_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().test(type))
                .sorted(Comparator.comparingInt(value -> value.getKey().priority()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static void registerTypeAdapter(TypeMatcher matcher, TypeAdapter adapter) {
        if (ADAPTER_MAP.put(matcher, adapter) != null) {
            throw new IllegalArgumentException("Duplicate type matcher with id: " + matcher.getIdentifier());
        }
    }

    static {
        // primitives
        registerTypeAdapter(TypeMatcher.matchBoolean(), new BooleanValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchCharacter(), new CharValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchInteger(), new IntValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchLong(), new LongValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchFloat(), new FloatValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchDouble(), new DoubleValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchString(), new StringValue.Adapter());

        // primitive arrays
        registerTypeAdapter(TypeMatcher.matchBooleanArray(), new BooleanArrayValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchIntegerArray(), new IntArrayValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchLongArray(), new LongArrayValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchFloatArray(), new FloatArrayValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchDoubleArray(), new DoubleArrayValue.Adapter());
        registerTypeAdapter(TypeMatcher.matchStringArray(), new StringArrayValue.Adapter());

        // enums
        registerTypeAdapter(TypeMatcher.matchEnum(), new EnumValue.Adapter<>());

        // objects
        registerTypeAdapter(TypeMatcher.matchObject(), new ObjectValue.Adapter());
    }
}
