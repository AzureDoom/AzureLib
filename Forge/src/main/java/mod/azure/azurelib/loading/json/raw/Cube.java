/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public class Cube {

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
        return inflate;
    }

    public Boolean mirror() {
        return mirror;
    }

    public double[] origin() {
        return origin;
    }

    public double[] pivot() {
        return pivot;
    }

    public double[] rotation() {
        return rotation;
    }

    public double[] size() {
        return size;
    }

    public UVUnion uv() {
        return uv;
    }

    public static JsonDeserializer<Cube> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
            Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
            double[] origin = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "origin", null));
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "pivot", null));
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "rotation", null));
            double[] size = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "size", null));
            UVUnion uvUnion = JSONUtils.getAsObject(obj, "uv", null, context, UVUnion.class);

            return new Cube(inflate, mirror, origin, pivot, rotation, size, uvUnion);
        };
    }
}
