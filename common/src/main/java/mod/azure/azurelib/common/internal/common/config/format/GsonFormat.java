package mod.azure.azurelib.common.internal.common.config.format;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import mod.azure.azurelib.common.internal.common.config.ConfigUtils;
import mod.azure.azurelib.common.internal.common.config.value.ConfigValue;
import mod.azure.azurelib.common.internal.common.config.value.IDescriptionProvider;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigReadException;
import mod.azure.azurelib.common.internal.common.config.exception.ConfigValueMissingException;
import mod.azure.azurelib.common.internal.common.config.io.ConfigIO;

public final class GsonFormat implements IConfigFormat {

    private final Gson gson;
    private final JsonObject root;

    public GsonFormat(Settings settings) {
        this.gson = settings.builder.create();
        this.root = new JsonObject();
    }

    private GsonFormat(JsonObject root) {
        this.root = root;
        this.gson = null; // no need to propagate
    }

    @Override
    public void writeBoolean(String field, boolean value) {
        this.root.addProperty(field, value);
    }

    @Override
    public boolean readBoolean(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsBoolean);
    }

    @Override
    public void writeChar(String field, char value) {
        this.root.addProperty(field, value);
    }

    @Override
    public char readChar(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsCharacter);
    }

    @Override
    public void writeInt(String field, int value) {
        this.root.addProperty(field, value);
    }

    @Override
    public int readInt(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsInt);
    }

    @Override
    public void writeLong(String field, long value) {
        this.root.addProperty(field, value);
    }

    @Override
    public long readLong(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsLong);
    }

    @Override
    public void writeFloat(String field, float value) {
        this.root.addProperty(field, value);
    }

    @Override
    public float readFloat(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsFloat);
    }

    @Override
    public void writeDouble(String field, double value) {
        this.root.addProperty(field, value);
    }

    @Override
    public double readDouble(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsDouble);
    }

    @Override
    public void writeString(String field, String value) {
        this.root.addProperty(field, value);
    }

    @Override
    public String readString(String field) throws ConfigValueMissingException {
        return this.tryRead(field, JsonElement::getAsString);
    }

    @Override
    public void writeBoolArray(String field, boolean[] values) {
        JsonArray array = new JsonArray();
        for (boolean b : values) {
            array.add(b);
        }
        this.root.add(field, array);
    }

    // I love Java primitive types (:
    @Override
    public boolean[] readBoolArray(String field) throws ConfigValueMissingException {
        return ConfigUtils.unboxArray(this.readArray(field, Boolean[]::new, JsonElement::getAsBoolean));
    }

    @Override
    public void writeIntArray(String field, int[] values) {
        JsonArray array = new JsonArray();
        for (int i : values) {
            array.add(i);
        }
        this.root.add(field, array);
    }

    @Override
    public int[] readIntArray(String field) throws ConfigValueMissingException {
        return ConfigUtils.unboxArray(this.readArray(field, Integer[]::new, JsonElement::getAsInt));
    }

    @Override
    public void writeLongArray(String field, long[] values) {
        JsonArray array = new JsonArray();
        for (long i : values) {
            array.add(i);
        }
        this.root.add(field, array);
    }

    @Override
    public long[] readLongArray(String field) throws ConfigValueMissingException {
        return ConfigUtils.unboxArray(this.readArray(field, Long[]::new, JsonElement::getAsLong));
    }

    @Override
    public void writeFloatArray(String field, float[] values) {
        JsonArray array = new JsonArray();
        for (float i : values) {
            array.add(i);
        }
        this.root.add(field, array);
    }

    @Override
    public float[] readFloatArray(String field) throws ConfigValueMissingException {
        return ConfigUtils.unboxArray(this.readArray(field, Float[]::new, JsonElement::getAsFloat));
    }

    @Override
    public void writeDoubleArray(String field, double[] values) {
        JsonArray array = new JsonArray();
        for (double i : values) {
            array.add(i);
        }
        this.root.add(field, array);
    }

    @Override
    public double[] readDoubleArray(String field) throws ConfigValueMissingException {
        return ConfigUtils.unboxArray(this.readArray(field, Double[]::new, JsonElement::getAsDouble));
    }

    @Override
    public void writeStringArray(String field, String[] values) {
        this.writeArray(field, values, JsonArray::add);
    }

    @Override
    public String[] readStringArray(String field) throws ConfigValueMissingException {
        return this.readArray(field, String[]::new, JsonElement::getAsString);
    }

    @Override
    public <E extends Enum<E>> void writeEnum(String field, E value) {
        this.root.addProperty(field, value.name());
    }

    @Override
    public <E extends Enum<E>> E readEnum(String field, Class<E> enumClass) throws ConfigValueMissingException {
        String value = readString(field);
        return ConfigUtils.getEnumConstant(value, enumClass);
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
        GsonFormat config = new GsonFormat(new Settings());
        value.values().forEach(val -> val.serializeValue(config));
        this.root.add(field, config.root);
    }

    @Override
    public void readMap(String field, Collection<ConfigValue<?>> values) throws ConfigValueMissingException {
        JsonElement element = this.root.get(field);
        if (element == null || !element.isJsonObject())
            throw new ConfigValueMissingException("Missing config value: " + field);
        JsonObject object = element.getAsJsonObject();
        GsonFormat config = new GsonFormat(object);
        for (ConfigValue<?> value : values) {
            value.deserializeValue(config);
        }
    }

    @Override
    public void writeFile(File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(gson.toJson(this.root));
        }
    }

    @Override
    public void readFile(File file) throws IOException, ConfigReadException {
        try (FileReader reader = new FileReader(file)) {
            JsonParser parser = new JsonParser();
            try {
                JsonElement element = parser.parse(reader);
                if (!element.isJsonObject()) {
                    throw new ConfigReadException("Gson config must contain JsonObject as root element!");
                }
                JsonObject object = element.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                    this.root.add(entry.getKey(), entry.getValue());
                }
            } catch (JsonParseException e) {
                throw new ConfigReadException("Config read failed", e);
            }
        }
    }

    @Override
    public void addComments(IDescriptionProvider provider) {
        // comments are not supported for JSON4 files
    }

    private <T> void writeArray(String field, T[] array, BiConsumer<JsonArray, T> elementConsumer) {
        JsonArray ar = new JsonArray();
        for (T t : array) {
            elementConsumer.accept(ar, t);
        }
        this.root.add(field, ar);
    }

    private <T> T tryRead(String field, Function<JsonElement, T> function) throws ConfigValueMissingException {
        JsonElement element = this.root.get(field);
        if (element == null) {
            throw new ConfigValueMissingException("Missing value: " + field);
        }
        try {
            return function.apply(element);
        } catch (Exception e) {
        	AzureLib.LOGGER.error(ConfigIO.MARKER, "Error loading value for field {} - {}", field, e);
            throw new ConfigValueMissingException("Invalid value");
        }
    }

    private <T> T[] readArray(String field, Function<Integer, T[]> arrayFactory, Function<JsonElement, T> function) throws ConfigValueMissingException {
        JsonElement element = this.root.get(field);
        if (element == null || !element.isJsonArray()) {
            throw new ConfigValueMissingException("Missing value: " + field);
        }
        JsonArray array = element.getAsJsonArray();
        T[] arr = arrayFactory.apply(array.size());
        try {
            int j = 0;
            for (JsonElement el : array) {
                arr[j++] = function.apply(el);
            }
            return arr;
        } catch (Exception e) {
        	AzureLib.LOGGER.error(ConfigIO.MARKER, "Error loading value for field {} - {}", field, e);
            throw new ConfigValueMissingException("Invalid value");
        }
    }

    /**
     * Settings holder for JSON configs
     *
     * @author Toma
     */
    public static final class Settings {

        private final GsonBuilder builder = new GsonBuilder();

        /**
         * Default settings constructor
         */
        public Settings() {
            this.builder.setPrettyPrinting().disableHtmlEscaping();
        }

        /**
         * Constructs new settings and allows you to customize {@link GsonBuilder} object
         * @param consumer Consumer of {@link GsonBuilder} for this settings object
         */
        public Settings(Consumer<GsonBuilder> consumer) {
            consumer.accept(builder);
        }
    }
}
