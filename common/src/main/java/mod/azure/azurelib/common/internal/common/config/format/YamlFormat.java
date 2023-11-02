package mod.azure.azurelib.common.internal.common.config.format;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.config.value.IDescriptionProvider;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigReadException;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class YamlFormat implements IConfigFormat {

    // writing
    private final StringBuilder buffer;
    private final int currentNesting;

    // reading
    private final Map<String, Object> processedData;
    private int readerIndex;


    public YamlFormat() {
        this(new HashMap<>());
    }

    public YamlFormat(StringBuilder buffer, int nesting) {
        this.buffer = buffer;
        this.currentNesting = nesting;
        this.processedData = new HashMap<>();
    }

    public YamlFormat(Map<String, Object> processed) {
        this.buffer = new StringBuilder();
        this.currentNesting = 0;
        this.processedData = processed;
    }

    @Override
    public void writeBoolean(String field, boolean value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public boolean readBoolean(String field) throws ConfigValueMissingException {
        return getValue(field, Boolean::parseBoolean);
    }

    @Override
    public void writeChar(String field, char value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public char readChar(String field) throws ConfigValueMissingException {
        return getValue(field, str -> str.charAt(0));
    }

    @Override
    public void writeInt(String field, int value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public int readInt(String field) throws ConfigValueMissingException {
        return getValue(field, Integer::parseInt);
    }

    @Override
    public void writeLong(String field, long value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public long readLong(String field) throws ConfigValueMissingException {
        return getValue(field, Long::parseLong);
    }

    @Override
    public void writeFloat(String field, float value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public float readFloat(String field) throws ConfigValueMissingException {
        return getValue(field, Float::parseFloat);
    }

    @Override
    public void writeDouble(String field, double value) {
        writeValuePair(field, String.valueOf(value));
    }

    @Override
    public double readDouble(String field) throws ConfigValueMissingException {
        return getValue(field, Double::parseDouble);
    }

    @Override
    public void writeString(String field, String value) {
        writeValuePair(field, value);
    }

    @Override
    public String readString(String field) throws ConfigValueMissingException {
        return getValue(field, Function.identity());
    }

    @Override
    public void writeBoolArray(String field, boolean[] values) {
        writeKey(field);
        for (boolean value : values) {
            writeArrayEntry(String.valueOf(value));
        }
        newLine();
    }

    @Override
    public boolean[] readBoolArray(String field) throws ConfigValueMissingException {
        String[] arr = this.getValueArray(field);
        boolean[] res = new boolean[arr.length];
        for (int i = 0; i < arr.length; i++) {
            res[i] = Boolean.parseBoolean(arr[i]);
        }
        return res;
    }

    @Override
    public void writeIntArray(String field, int[] values) {
        writeKey(field);
        for (int value : values) {
            writeArrayEntry(String.valueOf(value));
        }
        newLine();
    }

    @Override
    public int[] readIntArray(String field) throws ConfigValueMissingException {
        String[] arr = this.getValueArray(field);
        int[] res = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                res[i] = Integer.parseInt(arr[i]);
            } catch (NumberFormatException e) {
                throw new ConfigValueMissingException("Invalid value: " + field);
            }
        }
        return res;
    }

    @Override
    public void writeLongArray(String field, long[] values) {
        writeKey(field);
        for (long value : values) {
            writeArrayEntry(String.valueOf(value));
        }
        newLine();
    }

    @Override
    public long[] readLongArray(String field) throws ConfigValueMissingException {
        String[] arr = this.getValueArray(field);
        long[] res = new long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                res[i] = Long.parseLong(arr[i]);
            } catch (NumberFormatException e) {
                throw new ConfigValueMissingException("Invalid value: " + field);
            }
        }
        return res;
    }

    @Override
    public void writeFloatArray(String field, float[] values) {
        writeKey(field);
        for (float value : values) {
            writeArrayEntry(String.valueOf(value));
        }
        newLine();
    }

    @Override
    public float[] readFloatArray(String field) throws ConfigValueMissingException {
        String[] arr = this.getValueArray(field);
        float[] res = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                res[i] = Float.parseFloat(arr[i]);
            } catch (NumberFormatException e) {
                throw new ConfigValueMissingException("Invalid value: " + field);
            }
        }
        return res;
    }

    @Override
    public void writeDoubleArray(String field, double[] values) {
        writeKey(field);
        for (double value : values) {
            writeArrayEntry(String.valueOf(value));
        }
        newLine();
    }

    @Override
    public double[] readDoubleArray(String field) throws ConfigValueMissingException {
        String[] arr = this.getValueArray(field);
        double[] res = new double[arr.length];
        for (int i = 0; i < arr.length; i++) {
            try {
                res[i] = Double.parseDouble(arr[i]);
            } catch (NumberFormatException e) {
                throw new ConfigValueMissingException("Invalid value: " + field);
            }
        }
        return res;
    }

    @Override
    public void writeStringArray(String field, String[] values) {
        writeKey(field);
        for (String value : values) {
            writeArrayEntry(value);
        }
        newLine();
    }

    @Override
    public String[] readStringArray(String field) throws ConfigValueMissingException {
        return this.getValueArray(field);
    }

    @Override
    public <E extends Enum<E>> void writeEnum(String field, E value) {
        writeValuePair(field, value.name());
    }

    @Override
    public <E extends Enum<E>> E readEnum(String field, Class<E> enumClass) throws ConfigValueMissingException {
        String name = this.readString(field);
        return ConfigUtils.getEnumConstant(name, enumClass);
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
        writeKey(field);
        YamlFormat format = new YamlFormat(this.buffer, this.currentNesting + 1);
        value.values().forEach(val -> val.serializeValue(format));
    }

    @Override
    public void readMap(String field, Collection<ConfigValue<?>> values) throws ConfigValueMissingException {
        Map<String, Object> map = this.getValueMap(field);
        YamlFormat format = new YamlFormat(map);
        values.forEach(val -> val.deserializeValue(format));
    }

    @Override
    public void readFile(File file) throws IOException, ConfigReadException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String editedText = line.replaceAll("^[\\s|\\t]*#.+$", "");
                if (!editedText.isEmpty()) {
                    lines.add(editedText);
                }
            }
        }
        try {
            while (readerIndex < lines.size()) {
                this.process(lines);
            }
        } catch (Exception e) {
            throw new ConfigReadException("Config process failed", e);
        }
    }

    private void process(List<String> list) throws ConfigReadException {
        String value = list.get(readerIndex);
        String[] components = value.split(":\\s*", 2);
        Pattern pattern = Pattern.compile("^.+:\\s?\n?$");
        if (components.length == 1 || pattern.matcher(value).matches()) {
            // Arrays / objects
            if (readerIndex == list.size() - 1) {
                this.processedData.put(components[0].trim(), new String[0]);
                ++readerIndex;
                return;
            }
            String next = list.get(readerIndex + 1);
            if (next.trim().startsWith("-")) {
                this.processArray(list, components[0].trim());
            } else {
                this.processMap(list, components[0].trim());
            }
            return;
        } else if (components.length == 2) {
            // Primitives
            this.processedData.put(components[0].trim(), components[1].trim());
            ++readerIndex;
            return;
        }
        throw new ConfigReadException("Invalid config format");
    }

    private void processMap(List<String> list, String key) throws ConfigReadException {
        String prefix = "^ {2}";
        List<String> newValues = new ArrayList<>();
        while (readerIndex < list.size()) {
            int next = readerIndex + 1;
            if (next >= list.size())
                break;
            String value = list.get(next);
            if (value.startsWith("  ")) {
                newValues.add(value.replaceFirst(prefix, ""));
                ++readerIndex;
            } else {
                break;
            }
        }
        YamlFormat format = new YamlFormat(new HashMap<>());
        while (format.readerIndex < newValues.size()) {
            format.process(newValues);
        }
        this.processedData.put(key, format.processedData);
        ++this.readerIndex;
    }

    private void processArray(List<String> list, String key) {
        ++readerIndex;
        List<String> entries = new ArrayList<>();
        while (readerIndex < list.size()) {
            String entry = list.get(readerIndex).trim();
            if (!entry.startsWith("-"))
                break;
            entries.add(entry.replaceAll("^-\\s", ""));
            ++readerIndex;
        }
        this.processedData.put(key, entries.toArray(new String[0]));
    }

    @Override
    public void writeFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(this.buffer.toString());
        }
    }

    @Override
    public void addComments(IDescriptionProvider provider) {
        for (String comment : provider.getDescription()) {
            spaces();
            buffer.append("# ").append(comment).append("\n");
        }
    }

    private void spaces() {
        this.spaces(this.currentNesting);
    }

    private void spaces(int nestIndex) {
        if (nestIndex > 0) {
            for (int i = 0; i < nestIndex * 2; i++) {
                buffer.append(" ");
            }
        }
    }

    private void writeKey(String key) {
        spaces();
        buffer.append(key).append(":\n");
    }

    private void newLine() {
        buffer.append("\n");
    }

    private void writeArrayEntry(String value) {
        spaces(this.currentNesting + 1);
        buffer.append("- ").append(value).append("\n");
    }

    private void writeValuePair(String key, String value) {
        spaces();
        buffer.append(key).append(": ").append(value).append("\n\n");
    }

    private <V> V getValue(String key, Function<String, V> parser) throws ConfigValueMissingException {
        Object value = this.processedData.get(key);
        if (value == null) {
            throw new ConfigValueMissingException("Missing value: " + key);
        }
        try {
            return parser.apply(value.toString());
        } catch (ClassCastException | NumberFormatException e) {
            throw new ConfigValueMissingException("Value parse failed: " + key + ", value: " + value);
        }
    }

    private String[] getValueArray(String key) throws ConfigValueMissingException {
        Object value = this.processedData.get(key);
        if (value == null) {
            throw new ConfigValueMissingException("Missing value: " + key);
        }
        try {
            if (value instanceof Map) {
                return new String[0];
            }
            return (String[]) value;
        } catch (ClassCastException e) {
            throw new ConfigValueMissingException("Value parse failed: " + key + ", value: " + value);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getValueMap(String key) throws ConfigValueMissingException {
        Object value = this.processedData.get(key);
        if (value == null) {
            throw new ConfigValueMissingException("Missing value: " + key);
        }
        try {
            if (value instanceof String[]) {
                return new HashMap<>();
            }
            return (Map<String, Object>) value;
        } catch (ClassCastException e) {
            throw new ConfigValueMissingException("Value parse failed: " + key + ", value: " + value);
        }
    }
}
