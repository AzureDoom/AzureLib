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
 * Container class for poly mesh information, only used in deserialization at startup
 */
public class PolyMesh {

    private final Boolean normalizedUVs;

    private final double[] normals;

    private final PolysUnion polysUnion;

    private final double[] positions;

    private final double[] uvs;

    public PolyMesh(Boolean normalizedUVs, double[] normals, PolysUnion polysUnion, double[] positions, double[] uvs) {
        this.normalizedUVs = normalizedUVs;
        this.normals = normals;
        this.polysUnion = polysUnion;
        this.positions = positions;
        this.uvs = uvs;
    }

    public Boolean normalizedUVs() {
        return normalizedUVs;
    }

    public double[] normals() {
        return normals;
    }

    public PolysUnion polysUnion() {
        return polysUnion;
    }

    public double[] positions() {
        return positions;
    }

    public double[] uvs() {
        return uvs;
    }

    public static JsonDeserializer<PolyMesh> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Boolean normalizedUVs = JsonUtil.getOptionalBoolean(obj, "normalized_uvs");
            double[] normals = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "normals", null));
            PolysUnion polysUnion = JSONUtils.getAsObject(obj, "polys", null, context, PolysUnion.class);
            double[] positions = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "positions", null));
            double[] uvs = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uvs", null));

            return new PolyMesh(normalizedUVs, normals, polysUnion, positions, uvs);
        };
    }
}
