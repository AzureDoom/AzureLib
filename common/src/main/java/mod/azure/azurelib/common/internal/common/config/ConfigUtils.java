package mod.azure.azurelib.common.internal.common.config;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import mod.azure.azurelib.common.internal.common.AzureLib;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;
import net.minecraft.client.gui.components.EditBox;

public final class ConfigUtils {

    public static final char[] INTEGER_CHARS = { '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final char[] DECIMAL_CHARS = { '-', '.', 'E', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");
    public static final Pattern DECIMAL_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?(E[0-9]+)?");
    public static final Map<Class<?>, Class<?>> PRIMITIVE_MAPPINGS = new HashMap<>();

    public static void logCorrectedMessage(String field, @Nullable Object prevValue, Object corrected) {
    	AzureLib.LOGGER.warn(ConfigIO.MARKER, "Correcting config value '{}' from '{}' to '{}'", field, Objects.toString(prevValue), corrected);
    }

    public static void logArraySizeCorrectedMessage(String field, Object prevValue, Object corrected) {
    	AzureLib.LOGGER.warn(ConfigIO.MARKER, "Correcting config array value '{}' due to invalid size from '{}' to '{}'", field, prevValue, corrected);
    }

    public static boolean[] unboxArray(Boolean[] values) {
        boolean[] primitive = new boolean[values.length];
        int i = 0;
        for (boolean v : values) {
            primitive[i++] = v;
        }
        return primitive;
    }

    public static int[] unboxArray(Integer[] values) {
        int[] primitive = new int[values.length];
        int i = 0;
        for (int v : values) {
            primitive[i++] = v;
        }
        return primitive;
    }

    public static long[] unboxArray(Long[] values) {
        long[] primitive = new long[values.length];
        int i = 0;
        for (long v : values) {
            primitive[i++] = v;
        }
        return primitive;
    }

    public static float[] unboxArray(Float[] values) {
        float[] primitive = new float[values.length];
        int i = 0;
        for (float v : values) {
            primitive[i++] = v;
        }
        return primitive;
    }

    public static double[] unboxArray(Double[] values) {
        double[] primitive = new double[values.length];
        int i = 0;
        for (double v : values) {
            primitive[i++] = v;
        }
        return primitive;
    }

    public static <E extends Enum<E>> E getEnumConstant(String value, Class<E> declaringClass) throws ConfigValueMissingException {
        E[] constants = declaringClass.getEnumConstants();
        for (E e : constants) {
            if (e.name().equals(value)) {
                return e;
            }
        }
        throw new ConfigValueMissingException("Missing enum value: " + value);
    }

    public static boolean containsOnlyValidCharacters(String in, char[] allowedChars) {
        char[] arr = in.toCharArray();
        for (char c : arr) {
            boolean valid = false;
            for (char validate : allowedChars) {
                if (validate == c) {
                    valid = true;
                    break;
                }
            }
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    public static DecimalFormat getDecimalFormat(Field field) {
        Configurable.Gui.NumberFormat format = field.getAnnotation(Configurable.Gui.NumberFormat.class);
        if (format != null) {
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setDecimalSeparator('.');
            return new DecimalFormat(format.value(), symbols);
        }
        return null;
    }

    public static Class<?> remapPrimitiveType(Class<?> type) {
        return PRIMITIVE_MAPPINGS.getOrDefault(type, type);
    }

    public static void adjustCharacterLimit(Field field, EditBox widget) {
        Configurable.Gui.CharacterLimit limit = field.getAnnotation(Configurable.Gui.CharacterLimit.class);
        if (limit != null) {
            widget.setMaxLength(Math.max(limit.value(), 1));
        }
    }

    static {
        PRIMITIVE_MAPPINGS.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_MAPPINGS.put(Character.class, Character.TYPE);
        PRIMITIVE_MAPPINGS.put(Byte.class, Byte.TYPE);
        PRIMITIVE_MAPPINGS.put(Short.class, Short.TYPE);
        PRIMITIVE_MAPPINGS.put(Integer.class, Integer.TYPE);
        PRIMITIVE_MAPPINGS.put(Long.class, Long.TYPE);
        PRIMITIVE_MAPPINGS.put(Float.class, Float.TYPE);
        PRIMITIVE_MAPPINGS.put(Double.class, Double.TYPE);
    }

    private ConfigUtils() {
        throw new UnsupportedOperationException();
    }
}
