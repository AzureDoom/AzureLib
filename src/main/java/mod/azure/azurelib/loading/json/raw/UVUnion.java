/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for UV information, only used in deserialization at startup
 */
public final class UVUnion {

    private final double[] boxUVCoords;

    private final UVFaces faceUV;

    private final boolean isBoxUV;

    public UVUnion(double[] boxUVCoords, UVFaces faceUV, boolean isBoxUV) {
        this.boxUVCoords = boxUVCoords;
        this.faceUV = faceUV;
        this.isBoxUV = isBoxUV;
    }

    public double[] boxUVCoords() {
        return this.boxUVCoords;
    }

    public UVFaces faceUV() {
        return this.faceUV;
    }

    public boolean isBoxUV() {
        return this.isBoxUV;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UVUnion))
            return false;
        UVUnion other = (UVUnion) o;
        return java.util.Objects.equals(this.boxUVCoords, other.boxUVCoords)
            && java.util.Objects.equals(this.faceUV, other.faceUV)
            && this.isBoxUV == other.isBoxUV;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.boxUVCoords);
        result = 31 * result + java.util.Objects.hashCode(this.faceUV);
        result = 31 * result + Boolean.hashCode(this.isBoxUV);
        return result;
    }

    @Override
    public String toString() {
        return "UVUnion[boxUVCoords=" + this.boxUVCoords + ", faceUV=" + this.faceUV + ", isBoxUV=" + this.isBoxUV
            + "]";
    }

    public static JsonDeserializer<UVUnion> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            if (json.isJsonObject()) {
                return new UVUnion(new double[0], context.deserialize(json.getAsJsonObject(), UVFaces.class), false);
            } else if (json.isJsonArray()) {
                return new UVUnion(JsonUtil.jsonArrayToDoubleArray(json.getAsJsonArray()), null, true);
            } else {
                throw new JsonParseException(
                    "Invalid format provided for UVUnion, must be either double array or UVFaces collection"
                );
            }
        };
    }
}
