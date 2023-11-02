package mod.azure.azurelib.common.internal.common.config.format;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.config.value.IDescriptionProvider;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.config.exception.ConfigReadException;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;

public final class PropertiesFormat implements IConfigFormat {

    private final Settings settings;
    private final StringBuilder buffer;
    @Nullable
    private final String prefix;
    private final Map<String, String> parsed;

    public PropertiesFormat(Settings settings) {
        this(null, new StringBuilder(), settings);
    }

    private PropertiesFormat(String prefix, StringBuilder bufferRef, Settings settings) {
        this(prefix, bufferRef, new HashMap<>(), settings);
    }

    private PropertiesFormat(String prefix, StringBuilder bufferRef, Map<String, String> parsed, Settings settings) {
        this.prefix = prefix;
        this.buffer = bufferRef;
        this.parsed = parsed;
        this.settings = settings;
    }

    @Override
    public void writeBoolean(String field, boolean value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public boolean readBoolean(String field) throws ConfigValueMissingException {
        return this.parse(field, Boolean::parseBoolean);
    }

    @Override
    public void writeChar(String field, char value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public char readChar(String field) throws ConfigValueMissingException {
        return this.parse(field, s -> s.charAt(0));
    }

    @Override
    public void writeInt(String field, int value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public int readInt(String field) throws ConfigValueMissingException {
        return this.parse(field, Integer::parseInt);
    }

    @Override
    public void writeLong(String field, long value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public long readLong(String field) throws ConfigValueMissingException {
        return this.parse(field, Long::parseLong);
    }

    @Override
    public void writeFloat(String field, float value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public float readFloat(String field) throws ConfigValueMissingException {
        return this.parse(field, Float::parseFloat);
    }

    @Override
    public void writeDouble(String field, double value) {
        this.writePair(field, String.valueOf(value));
    }

    @Override
    public double readDouble(String field) throws ConfigValueMissingException {
        return this.parse(field, Double::parseDouble);
    }

    @Override
    public void writeString(String field, String value) {
        this.writePair(field, value);
    }

    @Override
    public String readString(String field) throws ConfigValueMissingException {
        return this.parse(field, Function.identity());
    }

    @Override
    public void writeBoolArray(String field, boolean[] values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (boolean value : values) {
            strings[i++] = String.valueOf(value);
        }
        this.writePair(field, String.join(settings.arraySeparator, strings));
    }

    @Override
    public boolean[] readBoolArray(String field) throws ConfigValueMissingException {
        String[] strings = this.getStringArray(field);
        boolean[] values = new boolean[strings.length];
        int i = 0;
        for (String string : strings) {
            try {
                values[i++] = Boolean.parseBoolean(string);
            } catch (Exception e) {
                throw new ConfigValueMissingException("Invalid value: " + string);
            }
        }
        return values;
    }

    @Override
    public void writeIntArray(String field, int[] values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (int value : values) {
            strings[i++] = String.valueOf(value);
        }
        this.writePair(field, String.join(settings.arraySeparator, strings));
    }

    @Override
    public int[] readIntArray(String field) throws ConfigValueMissingException {
        String[] strings = this.getStringArray(field);
        int[] values = new int[strings.length];
        int i = 0;
        for (String string : strings) {
            try {
                values[i++] = Integer.parseInt(string);
            } catch (Exception e) {
                throw new ConfigValueMissingException("Invalid value: " + string);
            }
        }
        return values;
    }

    @Override
    public void writeLongArray(String field, long[] values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (long value : values) {
            strings[i++] = String.valueOf(value);
        }
        this.writePair(field, String.join(settings.arraySeparator, strings));
    }

    @Override
    public long[] readLongArray(String field) throws ConfigValueMissingException {
        String[] strings = this.getStringArray(field);
        long[] values = new long[strings.length];
        int i = 0;
        for (String string : strings) {
            try {
                values[i++] = Long.parseLong(string);
            } catch (Exception e) {
                throw new ConfigValueMissingException("Invalid value: " + string);
            }
        }
        return values;
    }

    @Override
    public void writeFloatArray(String field, float[] values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (float value : values) {
            strings[i++] = String.valueOf(value);
        }
        this.writePair(field, String.join(settings.arraySeparator, strings));
    }

    @Override
    public float[] readFloatArray(String field) throws ConfigValueMissingException {
        String[] strings = this.getStringArray(field);
        float[] values = new float[strings.length];
        int i = 0;
        for (String string : strings) {
            try {
                values[i++] = Float.parseFloat(string);
            } catch (Exception e) {
                throw new ConfigValueMissingException("Invalid value: " + string);
            }
        }
        return values;
    }

    @Override
    public void writeDoubleArray(String field, double[] values) {
        String[] strings = new String[values.length];
        int i = 0;
        for (double value : values) {
            strings[i++] = String.valueOf(value);
        }
        this.writePair(field, String.join(settings.arraySeparator, strings));
    }

    @Override
    public double[] readDoubleArray(String field) throws ConfigValueMissingException {
        String[] strings = this.getStringArray(field);
        double[] values = new double[strings.length];
        int i = 0;
        for (String string : strings) {
            try {
                values[i++] = Double.parseDouble(string);
            } catch (Exception e) {
                throw new ConfigValueMissingException("Invalid value: " + string);
            }
        }
        return values;
    }

    @Override
    public void writeStringArray(String field, String[] values) {
        this.writePair(field, String.join(settings.arraySeparator, values));
    }

    @Override
    public String[] readStringArray(String field) throws ConfigValueMissingException {
        return this.getStringArray(field);
    }

    @Override
    public <E extends Enum<E>> void writeEnum(String field, E value) {
        this.writePair(field, value.name());
    }

    @Override
    public <E extends Enum<E>> E readEnum(String field, Class<E> enumClass) throws ConfigValueMissingException {
        return ConfigUtils.getEnumConstant(this.getValue(field), enumClass);
    }

    @Override
    public <E extends Enum<E>> void writeEnumArray(String field, E[] value) {
        String[] strings = Arrays.stream(value).map(Enum::name).toArray(String[]::new);
        writeStringArray(field, strings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends Enum<E>> E[] readEnumArray(String field, Class<E> enumClass) throws ConfigValueMissingException {
        String[] strings = readStringArray(field);
        E[] arr = (E[]) Array.newInstance(enumClass, strings.length);
        for (int i = 0; i < strings.length; i++) {
            arr[i] = ConfigUtils.getEnumConstant(strings[i], enumClass);
        }
        return arr;
    }

    @Override
    public void writeMap(String field, Map<String, ConfigValue<?>> value) {
        String prefix = this.prefix != null ? this.prefix + "." + field : field;
        PropertiesFormat format = new PropertiesFormat(prefix, this.buffer, this.settings);
        value.values().forEach(val -> val.serializeValue(format));
    }

    @Override
    public void readMap(String field, Collection<ConfigValue<?>> values) throws ConfigValueMissingException {
        Set<String> validElements = this.parsed.keySet()
                .stream()
                .filter(key -> {
                    String[] strings = key.split("\\.", 2);
                    if (strings.length < 2) {
                        return false;
                    }
                    String prefix = strings[0];
                    return prefix.equals(field);
                })
                .collect(Collectors.toSet());
        Map<String, String> parsed = new HashMap<>();
        for (String key : validElements) {
            String s = key.split("\\.", 2)[1];
            parsed.put(s, this.getValue(key));
        }
        PropertiesFormat format = new PropertiesFormat(this.prefix, this.buffer, parsed, this.settings);
        for (ConfigValue<?> value : values) {
            value.deserializeValue(format);
        }
    }

    @Override
    public void readFile(File file) throws IOException, ConfigReadException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String filtered = line.replaceAll("#.+$", "");
                boolean isPair = filtered.contains("=");
                String[] components = filtered.split("=");
                if (components.length != 2) {
                    if (isPair) {
                        parsed.put(components[0], "");
                    }
                    continue;
                }
                parsed.put(components[0], components[1]);
            }
        }
    }

    @Override
    public void writeFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(this.buffer.toString());
        }
    }

    @Override
    public void addComments(IDescriptionProvider provider) {
        String[] comments = provider.getDescription();
        if (comments.length == 0) {
            return;
        }
        for (String string : comments) {
            this.buffer.append("# ").append(string).append("\n");
        }
    }

    private String getValue(String field) throws ConfigValueMissingException {
        String res = this.parsed.get(field);
        if (res == null) {
            throw new ConfigValueMissingException("Missing value " + field);
        }
        return res;
    }

    private <T> T parse(String s, Function<String, T> parser) throws ConfigValueMissingException {
        String val = this.getValue(s);
        try {
            return parser.apply(val);
        } catch (Exception e) {
            throw new ConfigValueMissingException("Value parse failed", e);
        }
    }

    private void writePair(String field, String value) {
        if (this.prefix != null) {
            this.buffer.append(this.prefix).append(".");
        }
        this.buffer.append(field).append("=").append(value);
        for (int i = 0; i < settings.newlines; i++) {
            this.buffer.append("\n");
        }
    }

    private String[] getStringArray(String field) throws ConfigValueMissingException {
        String value = this.getValue(field);
        return value.split(this.settings.arraySeparator);
    }

    /**
     * Settings holder for JSON configs
     *
     * @author Toma
     */
    public static final class Settings {

        private String arraySeparator = ";";
        private int newlines = 1;

        /**
         * Allows you to configure custom separator used for arrays in case the default
         * one is causing issues
         *
         * @param arraySeparator Nonnull separator to be used
         * @return This instance
         */
        public Settings arraySeparator(String arraySeparator) {
            this.arraySeparator = Objects.requireNonNull(arraySeparator);
            return this;
        }

        /**
         * Specifies amount of newlines after each value (Not comments)
         * @param count Count of newlines
         * @return This instance
         */
        public Settings newlines(int count) {
            this.newlines = Math.max(1, count);
            return this;
        }
    }
}
