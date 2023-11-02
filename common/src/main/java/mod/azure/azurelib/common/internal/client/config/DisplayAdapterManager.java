package mod.azure.azurelib.common.internal.client.config;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.common.internal.common.config.adapter.TypeMatcher;

public final class DisplayAdapterManager {

    private static final Map<TypeMatcher, DisplayAdapter> ADAPTER_MAP = new HashMap<>();

    public static DisplayAdapter forType(Class<?> type) {
        return ADAPTER_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().test(type))
                .sorted(Comparator.comparingInt(value -> value.getKey().priority()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public static void registerDisplayAdapter(TypeMatcher matcher, DisplayAdapter adapter) {
        if (ADAPTER_MAP.put(matcher, adapter) != null) {
            throw new IllegalArgumentException("Duplicate type matcher with id: " + matcher.getIdentifier());
        }
    }

    static {
        registerDisplayAdapter(TypeMatcher.matchBoolean(), DisplayAdapter.booleanValue());
        registerDisplayAdapter(TypeMatcher.matchCharacter(), DisplayAdapter.characterValue());
        registerDisplayAdapter(TypeMatcher.matchInteger(), DisplayAdapter.integerValue());
        registerDisplayAdapter(TypeMatcher.matchLong(), DisplayAdapter.longValue());
        registerDisplayAdapter(TypeMatcher.matchFloat(), DisplayAdapter.floatValue());
        registerDisplayAdapter(TypeMatcher.matchDouble(), DisplayAdapter.doubleValue());
        registerDisplayAdapter(TypeMatcher.matchString(), DisplayAdapter.stringValue());
        registerDisplayAdapter(TypeMatcher.matchBooleanArray(), DisplayAdapter.booleanArrayValue());
        registerDisplayAdapter(TypeMatcher.matchIntegerArray(), DisplayAdapter.integerArrayValue());
        registerDisplayAdapter(TypeMatcher.matchLongArray(), DisplayAdapter.longArrayValue());
        registerDisplayAdapter(TypeMatcher.matchFloatArray(), DisplayAdapter.floatArrayValue());
        registerDisplayAdapter(TypeMatcher.matchDoubleArray(), DisplayAdapter.doubleArrayValue());
        registerDisplayAdapter(TypeMatcher.matchStringArray(), DisplayAdapter.stringArrayValue());
        registerDisplayAdapter(TypeMatcher.matchEnum(), DisplayAdapter.enumValue());
        registerDisplayAdapter(TypeMatcher.matchObject(), DisplayAdapter.objectValue());
    }

    private DisplayAdapterManager() {
        throw new UnsupportedOperationException();
    }
}
