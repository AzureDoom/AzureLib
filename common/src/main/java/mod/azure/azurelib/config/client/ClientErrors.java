/**
 * This class is a fork of the matching class found in the Configuration repository. Original source:
 * https://github.com/Toma1O6/Configuration Copyright © 2024 Toma1O6. Licensed under the MIT License.
 */
package mod.azure.azurelib.config.client;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.regex.Pattern;

public final class ClientErrors {

    public static final MutableComponent CHAR_VALUE_EMPTY = Component.translatable(
        "text.azurelib.error.character_value_empty"
    );

    private static final String KEY_NAN = "text.azurelib.error.nan";

    private static final String KEY_NUM_BOUNDS = "text.azurelib.error.num_bounds";

    private static final String KEY_MISMATCHED_PATTERN = "text.azurelib.error.pattern_mismatch";

    private ClientErrors() {
        throw new UnsupportedOperationException();
    }

    public static MutableComponent notANumber(String value) {
        return Component.translatable(KEY_NAN, value);
    }

    public static MutableComponent outOfBounds(Number number, Number min, Number max) {
        return Component.translatable(KEY_NUM_BOUNDS, number, min, max);
    }

    public static MutableComponent invalidText(String text, Pattern pattern) {
        return Component.translatable(KEY_MISMATCHED_PATTERN, text, pattern.pattern());
    }
}
