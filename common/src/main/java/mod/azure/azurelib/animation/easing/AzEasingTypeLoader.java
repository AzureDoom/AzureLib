package mod.azure.azurelib.animation.easing;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.util.Locale;

public class AzEasingTypeLoader {

    /**
     * Retrieve an {@code EasingType} instance based on a {@link JsonElement}. Returns one of the default
     * {@code EasingTypes} if the name matches, or any other registered {@code EasingType} with a matching name.
     *
     * @param json The {@code easing} {@link JsonElement} to attempt to parse.
     * @return A usable {@code EasingType} instance
     */
    public static AzEasingType fromJson(JsonElement json) {
        if (!(json instanceof JsonPrimitive primitive) || !primitive.isString())
            return AzEasingTypes.LINEAR;

        return fromString(primitive.getAsString().toLowerCase(Locale.ROOT));
    }

    /**
     * Get an existing {@code EasingType} from a given string, matching the string to its name.
     *
     * @param name The name of the easing function
     * @return The relevant {@code EasingType}, or {@link AzEasingTypes#LINEAR} if none match
     */
    public static AzEasingType fromString(String name) {
        return AzEasingTypeRegistry.getOrDefault(name, AzEasingTypes.LINEAR);
    }
}
