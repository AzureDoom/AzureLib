package mod.azure.azurelib.common.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

/**
 * Size limits and strict readers for mesh data, so a malformed or oversized mesh fails to load with a message that says
 * what is wrong and where, instead of an index exception at bake or render time.
 * <p>
 * Skinned meshes are deformed on the CPU every frame, so the limits are generous for hand-made models but stop runaway
 * files from stalling the game.
 * </p>
 */
public final class MeshValidation {

    public static final int MAX_POSITIONS = 65_536;

    public static final int MAX_ATTRIBUTES = 262_144;

    public static final int MAX_POLYGONS = 131_072;

    public static final int MAX_POLYGON_CORNERS = 64;

    public static final int MAX_JOINTS = 256;

    public static final int MAX_LISTED_INFLUENCES = 64;

    private MeshValidation() {}

    static JsonParseException error(String where, String message) {
        return new JsonParseException(where + ": " + message);
    }

    static void checkCount(String where, int count, int max) {
        if (count > max) {
            throw error(where, "has " + count + " entries, more than the limit of " + max);
        }
    }

    static JsonArray array(JsonElement element, String where) {
        if (element == null || !element.isJsonArray()) {
            throw error(where, "must be an array, got " + describe(element));
        }

        return element.getAsJsonArray();
    }

    static double finite(JsonElement element, String where) {
        if (!(element instanceof JsonPrimitive primitive) || !primitive.isNumber()) {
            throw error(where, "must be a number, got " + describe(element));
        }

        double value = primitive.getAsDouble();

        if (!Double.isFinite(value)) {
            throw error(where, "must be a finite number, got " + value);
        }

        return value;
    }

    static double finiteFloat(JsonElement element, String where) {
        double value = finite(element, where);

        if (Math.abs(value) > Float.MAX_VALUE) {
            throw error(where, "is too large, got " + value + " (the limit is " + Float.MAX_VALUE + ")");
        }

        return value;
    }

    static int index(JsonElement element, String where) {
        double value = finite(element, where);

        if (value != Math.rint(value) || value < 0 || value > Integer.MAX_VALUE) {
            throw error(where, "must be a whole number of 0 or more, got " + element);
        }

        return (int) value;
    }

    static double[][] vectors(JsonElement element, String where, int size, int max) {
        if (element == null || element.isJsonNull()) {
            return new double[0][];
        }

        JsonArray array = array(element, where);
        checkCount(where, array.size(), max);
        double[][] vectors = new double[array.size()][];

        for (int i = 0; i < vectors.length; i++) {
            String at = where + "[" + i + "]";
            JsonArray vector = array(array.get(i), at);

            if (vector.size() < size) {
                throw error(at, "must have " + size + " numbers, got " + vector.size());
            }

            vectors[i] = new double[size];

            for (int c = 0; c < size; c++) {
                vectors[i][c] = finiteFloat(vector.get(c), at);
            }
        }

        return vectors;
    }

    static void checkIndex(int value, int count, String where, String what) {
        if (value >= count) {
            throw error(where, what + " index " + value + " is out of range (the mesh has " + count + ")");
        }
    }

    private static String describe(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "nothing";
        }

        String text = element.toString();
        return text.length() > 40 ? text.substring(0, 40) + "..." : text;
    }
}
