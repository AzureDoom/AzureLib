/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public final class Cube {

    private final Double inflate;

    private final Boolean mirror;

    private final double[] origin;

    private final double[] pivot;

    private final double[] rotation;

    private final double[] size;

    private final UVUnion uv;

    public Cube(
        Double inflate,
        Boolean mirror,
        double[] origin,
        double[] pivot,
        double[] rotation,
        double[] size,
        UVUnion uv
    ) {
        this.inflate = inflate;
        this.mirror = mirror;
        this.origin = origin;
        this.pivot = pivot;
        this.rotation = rotation;
        this.size = size;
        this.uv = uv;
    }

    public Double inflate() {
        return this.inflate;
    }

    public Boolean mirror() {
        return this.mirror;
    }

    public double[] origin() {
        return this.origin;
    }

    public double[] pivot() {
        return this.pivot;
    }

    public double[] rotation() {
        return this.rotation;
    }

    public double[] size() {
        return this.size;
    }

    public UVUnion uv() {
        return this.uv;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cube))
            return false;
        Cube other = (Cube) o;
        return java.util.Objects.equals(this.inflate, other.inflate)
            && java.util.Objects.equals(this.mirror, other.mirror)
            && java.util.Objects.equals(this.origin, other.origin)
            && java.util.Objects.equals(this.pivot, other.pivot)
            && java.util.Objects.equals(this.rotation, other.rotation)
            && java.util.Objects.equals(this.size, other.size)
            && java.util.Objects.equals(this.uv, other.uv);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.inflate);
        result = 31 * result + java.util.Objects.hashCode(this.mirror);
        result = 31 * result + java.util.Objects.hashCode(this.origin);
        result = 31 * result + java.util.Objects.hashCode(this.pivot);
        result = 31 * result + java.util.Objects.hashCode(this.rotation);
        result = 31 * result + java.util.Objects.hashCode(this.size);
        result = 31 * result + java.util.Objects.hashCode(this.uv);
        return result;
    }

    @Override
    public String toString() {
        return "Cube[inflate=" + this.inflate + ", mirror=" + this.mirror + ", origin=" + this.origin + ", pivot="
            + this.pivot + ", rotation=" + this.rotation + ", size=" + this.size + ", uv=" + this.uv + "]";
    }

    public static JsonDeserializer<Cube> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
            Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
            double[] origin = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "origin", null));
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "pivot", null));
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
            double[] size = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "size", null));
            UVUnion uvUnion = GsonHelper.getAsObject(obj, "uv", null, context, UVUnion.class);

            return new Cube(inflate, mirror, origin, pivot, rotation, size, uvUnion);
        };
    }
}
