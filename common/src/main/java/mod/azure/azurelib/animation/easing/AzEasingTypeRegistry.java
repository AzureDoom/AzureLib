package mod.azure.azurelib.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class AzEasingTypeRegistry {

    private static final Map<String, AzEasingType> EASING_TYPES = new HashMap<>();

    private static String normalizeName(String name) {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Register an {@code EasingType} with AzureLib for handling animation transitions and value curves.<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     * It is recommended you don't call this directly, and instead call it via {@code AzureLibUtil#addCustomEasingType}
     *
     * @param name        The name of the easing type
     * @param transformer Builds the easing curve for a given easing argument (or {@code null} for none). The curve it
     *                    returns is cached per argument value and reused, so it must be a pure function of its input.
     * @return The {@code EasingType} you registered
     */
    public static AzEasingType register(String name, Function<Double, Double2DoubleFunction> transformer) {
        var normalizedName = normalizeName(name);

        return EASING_TYPES.computeIfAbsent(
            normalizedName,
            $ -> new AzTransformerEasingType(normalizedName, transformer)
        );
    }

    public static AzEasingType register(String name, AzEasingType easingType) {
        return EASING_TYPES.computeIfAbsent(normalizeName(name), $ -> easingType);
    }

    public static AzEasingType getOrDefault(String name, @NotNull AzEasingType defaultValue) {
        return EASING_TYPES.getOrDefault(normalizeName(name), defaultValue);
    }

    public static @Nullable AzEasingType getOrNull(String name) {
        return EASING_TYPES.get(normalizeName(name));
    }

    public static Collection<AzEasingType> getValues() {
        return Collections.unmodifiableCollection(EASING_TYPES.values());
    }
}
