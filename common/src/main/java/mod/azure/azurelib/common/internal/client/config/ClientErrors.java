package mod.azure.azurelib.common.internal.client.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Pattern;

import mod.azure.azurelib.common.internal.common.config.value.DecimalValue;
import mod.azure.azurelib.common.internal.common.config.value.IntegerValue;

public final class ClientErrors {

    public static final MutableComponent CHAR_VALUE_EMPTY = Component.translatable("text.azurelib.error.character_value_empty");

    private static final String KEY_NAN = "text.azurelib.error.nan";
    private static final String KEY_NUM_BOUNDS = "text.azurelib.error.num_bounds";
    private static final String KEY_MISMATCHED_PATTERN = "text.azurelib.error.pattern_mismatch";

    public static MutableComponent notANumber(String value) {
        return Component.translatable(KEY_NAN, value);
    }

    public static MutableComponent outOfBounds(int i, IntegerValue.Range range) {
        return Component.translatable(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static MutableComponent outOfBounds(long i, IntegerValue.Range range) {
        return Component.translatable(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static MutableComponent outOfBounds(float i, DecimalValue.Range range) {
        return Component.translatable(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static MutableComponent outOfBounds(double i, DecimalValue.Range range) {
        return Component.translatable(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static MutableComponent invalidText(String text, Pattern pattern) {
        return Component.translatable(KEY_MISMATCHED_PATTERN, text, pattern);
    }
}
