package mod.azure.azurelib.client;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.regex.Pattern;

import mod.azure.azurelib.config.value.DecimalValue;
import mod.azure.azurelib.config.value.IntegerValue;

public final class ClientErrors {

    public static final IFormattableTextComponent CHAR_VALUE_EMPTY = new TranslationTextComponent(
        "text.azurelib.error.character_value_empty"
    );

    private static final String KEY_NAN = "text.azurelib.error.nan";

    private static final String KEY_NUM_BOUNDS = "text.azurelib.error.num_bounds";

    private static final String KEY_MISMATCHED_PATTERN = "text.azurelib.error.pattern_mismatch";

    public static IFormattableTextComponent notANumber(String value) {
        return new TranslationTextComponent(KEY_NAN, value);
    }

    public static IFormattableTextComponent outOfBounds(int i, IntegerValue.Range range) {
        return new TranslationTextComponent(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static IFormattableTextComponent outOfBounds(long i, IntegerValue.Range range) {
        return new TranslationTextComponent(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static IFormattableTextComponent outOfBounds(float i, DecimalValue.Range range) {
        return new TranslationTextComponent(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static IFormattableTextComponent outOfBounds(double i, DecimalValue.Range range) {
        return new TranslationTextComponent(KEY_NUM_BOUNDS, i, range.min(), range.max());
    }

    public static IFormattableTextComponent invalidText(String text, Pattern pattern) {
        return new TranslationTextComponent(KEY_MISMATCHED_PATTERN, text, pattern);
    }
}
