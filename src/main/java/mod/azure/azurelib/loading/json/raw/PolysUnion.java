/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for poly union information, only used in deserialization at startup
 */
public final class PolysUnion {

    private final double[][][] union;

    private final Type type;

    public PolysUnion(double[][][] union, Type type) {
        this.union = union;
        this.type = type;
    }

    public double[][][] union() {
        return this.union;
    }

    public Type type() {
        return this.type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PolysUnion))
            return false;
        PolysUnion other = (PolysUnion) o;
        return java.util.Objects.equals(this.union, other.union)
            && java.util.Objects.equals(this.type, other.type);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.union);
        result = 31 * result + java.util.Objects.hashCode(this.type);
        return result;
    }

    @Override
    public String toString() {
        return "PolysUnion[union=" + this.union + ", type=" + this.type + "]";
    }

    public static JsonDeserializer<PolysUnion> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isString()) {
                return new PolysUnion(new double[0][0][0], context.deserialize(json.getAsJsonPrimitive(), Type.class));
            } else if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                double[][][] matrix = makeSizedMatrix(array);

                for (int x = 0; x < array.size(); x++) {
                    JsonArray xArray = array.get(x).getAsJsonArray();

                    for (int y = 0; y < xArray.size(); y++) {
                        JsonArray yArray = xArray.get(y).getAsJsonArray();

                        matrix[x][y] = JsonUtil.jsonArrayToDoubleArray(yArray);
                    }
                }

                return new PolysUnion(matrix, null);
            } else {
                throw new JsonParseException("Invalid format for PolysUnion, must be either string or array");
            }
        };
    }

    private static double[][][] makeSizedMatrix(JsonArray array) {
        JsonArray subArray = array.size() > 0 ? array.get(0).getAsJsonArray() : null;
        JsonArray subSubArray = subArray != null && subArray.size() > 0 ? subArray.get(0).getAsJsonArray() : null;
        int ySize = subArray != null ? subArray.size() : 0;
        int zSize = subSubArray != null ? subSubArray.size() : 0;

        return new double[array.size()][ySize][zSize];
    }

    public enum Type {
        @SerializedName(value = "quad_list")
        QUAD,
        @SerializedName(value = "tri_list")
        TRI;
    }
}
