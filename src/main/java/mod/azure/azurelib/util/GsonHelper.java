package mod.azure.azurelib.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import javax.annotation.Nullable;

/**
 * The subset of 1.18's {@code net.minecraft.util.GsonHelper} that AzureLib's JSON loading relies on. Re-implemented
 * against Gson 2.2.4 (the version bundled with Minecraft 1.7.10) so the parsing behaviour matches the modern branches
 * exactly.
 */
public final class GsonHelper {

    private GsonHelper() {
        throw new UnsupportedOperationException();
    }

    public static boolean isNumberValue(JsonObject json, String memberName) {
        return isValidPrimitive(json, memberName) && json.getAsJsonPrimitive(memberName).isNumber();
    }

    public static boolean isStringValue(JsonObject json, String memberName) {
        return isValidPrimitive(json, memberName) && json.getAsJsonPrimitive(memberName).isString();
    }

    public static boolean isValidPrimitive(JsonObject json, String memberName) {
        return isValidNode(json, memberName) && json.get(memberName).isJsonPrimitive();
    }

    public static boolean isValidNode(@Nullable JsonObject json, String memberName) {
        return json != null && json.get(memberName) != null;
    }

    public static String convertToString(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        }
        throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + getType(json));
    }

    public static String getAsString(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToString(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
    }

    @Nullable
    public static String getAsString(JsonObject json, String memberName, @Nullable String fallback) {
        return json.has(memberName) ? convertToString(json.get(memberName), memberName) : fallback;
    }

    public static boolean convertToBoolean(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsBoolean();
        }
        throw new JsonSyntaxException("Expected " + memberName + " to be a Boolean, was " + getType(json));
    }

    public static boolean getAsBoolean(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToBoolean(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Boolean");
    }

    public static boolean getAsBoolean(JsonObject json, String memberName, boolean fallback) {
        return json.has(memberName) ? convertToBoolean(json.get(memberName), memberName) : fallback;
    }

    private static JsonPrimitive numberPrimitive(JsonElement json, String memberName, String type) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return json.getAsJsonPrimitive();
        }
        throw new JsonSyntaxException("Expected " + memberName + " to be a " + type + ", was " + getType(json));
    }

    public static double convertToDouble(JsonElement json, String memberName) {
        return numberPrimitive(json, memberName, "Double").getAsDouble();
    }

    public static double getAsDouble(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToDouble(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Double");
    }

    public static double getAsDouble(JsonObject json, String memberName, double fallback) {
        return json.has(memberName) ? convertToDouble(json.get(memberName), memberName) : fallback;
    }

    public static float convertToFloat(JsonElement json, String memberName) {
        return numberPrimitive(json, memberName, "Float").getAsFloat();
    }

    public static float getAsFloat(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToFloat(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Float");
    }

    public static float getAsFloat(JsonObject json, String memberName, float fallback) {
        return json.has(memberName) ? convertToFloat(json.get(memberName), memberName) : fallback;
    }

    public static long convertToLong(JsonElement json, String memberName) {
        return numberPrimitive(json, memberName, "Long").getAsLong();
    }

    public static long getAsLong(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToLong(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Long");
    }

    public static long getAsLong(JsonObject json, String memberName, long fallback) {
        return json.has(memberName) ? convertToLong(json.get(memberName), memberName) : fallback;
    }

    public static int convertToInt(JsonElement json, String memberName) {
        return numberPrimitive(json, memberName, "Int").getAsInt();
    }

    public static int getAsInt(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToInt(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
    }

    public static int getAsInt(JsonObject json, String memberName, int fallback) {
        return json.has(memberName) ? convertToInt(json.get(memberName), memberName) : fallback;
    }

    public static JsonObject convertToJsonObject(JsonElement json, String memberName) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        }
        throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + getType(json));
    }

    public static JsonObject getAsJsonObject(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToJsonObject(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonObject");
    }

    @Nullable
    public static JsonObject getAsJsonObject(JsonObject json, String memberName, @Nullable JsonObject fallback) {
        return json.has(memberName) ? convertToJsonObject(json.get(memberName), memberName) : fallback;
    }

    public static JsonArray convertToJsonArray(JsonElement json, String memberName) {
        if (json.isJsonArray()) {
            return json.getAsJsonArray();
        }
        throw new JsonSyntaxException("Expected " + memberName + " to be a JsonArray, was " + getType(json));
    }

    public static JsonArray getAsJsonArray(JsonObject json, String memberName) {
        if (json.has(memberName)) {
            return convertToJsonArray(json.get(memberName), memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
    }

    @Nullable
    public static JsonArray getAsJsonArray(JsonObject json, String memberName, @Nullable JsonArray fallback) {
        return json.has(memberName) ? convertToJsonArray(json.get(memberName), memberName) : fallback;
    }

    public static <T> T convertToObject(
        @Nullable JsonElement json,
        String memberName,
        JsonDeserializationContext context,
        Class<? extends T> adapter
    ) {
        if (json != null) {
            return context.deserialize(json, adapter);
        }
        throw new JsonSyntaxException("Missing " + memberName);
    }

    @Nullable
    public static <T> T getAsObject(
        JsonObject json,
        String memberName,
        @Nullable T fallback,
        JsonDeserializationContext context,
        Class<? extends T> adapter
    ) {
        return json.has(memberName) ? convertToObject(json.get(memberName), memberName, context, adapter) : fallback;
    }

    public static <T> T getAsObject(
        JsonObject json,
        String memberName,
        JsonDeserializationContext context,
        Class<? extends T> adapter
    ) {
        if (json.has(memberName)) {
            return convertToObject(json.get(memberName), memberName, context, adapter);
        }
        throw new JsonSyntaxException("Missing " + memberName);
    }

    public static String getType(@Nullable JsonElement json) {
        String s = String.valueOf(json);
        if (s.length() > 10) {
            s = s.substring(0, 10) + "...";
        }
        if (json == null) {
            return "null (missing)";
        } else if (json.isJsonNull()) {
            return "NULL";
        } else if (json.isJsonArray()) {
            return "an array (" + s + ")";
        } else if (json.isJsonObject()) {
            return "an object (" + s + ")";
        } else {
            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = json.getAsJsonPrimitive();
                if (primitive.isNumber()) {
                    return "a number (" + s + ")";
                }
                if (primitive.isBoolean()) {
                    return "a boolean (" + s + ")";
                }
            }
            return s;
        }
    }

    @Nullable
    public static <T> T fromJson(Gson gson, String json, Class<T> type) {
        try {
            JsonReader reader = new JsonReader(new StringReader(json));
            reader.setLenient(false);
            return gson.getAdapter(type).read(reader);
        } catch (IOException e) {
            throw new JsonParseException(e);
        }
    }
}
