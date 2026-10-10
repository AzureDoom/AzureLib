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
 * Container class for poly mesh information, only used in deserialization at startup
 */
public final class PolyMesh {

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
        return this.normalizedUVs;
    }

    public double[] normals() {
        return this.normals;
    }

    public PolysUnion polysUnion() {
        return this.polysUnion;
    }

    public double[] positions() {
        return this.positions;
    }

    public double[] uvs() {
        return this.uvs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PolyMesh))
            return false;
        PolyMesh other = (PolyMesh) o;
        return java.util.Objects.equals(this.normalizedUVs, other.normalizedUVs)
            && java.util.Objects.equals(this.normals, other.normals)
            && java.util.Objects.equals(this.polysUnion, other.polysUnion)
            && java.util.Objects.equals(this.positions, other.positions)
            && java.util.Objects.equals(this.uvs, other.uvs);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.normalizedUVs);
        result = 31 * result + java.util.Objects.hashCode(this.normals);
        result = 31 * result + java.util.Objects.hashCode(this.polysUnion);
        result = 31 * result + java.util.Objects.hashCode(this.positions);
        result = 31 * result + java.util.Objects.hashCode(this.uvs);
        return result;
    }

    @Override
    public String toString() {
        return "PolyMesh[normalizedUVs=" + this.normalizedUVs + ", normals=" + this.normals + ", polysUnion="
            + this.polysUnion + ", positions=" + this.positions + ", uvs=" + this.uvs + "]";
    }

    public static JsonDeserializer<PolyMesh> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Boolean normalizedUVs = JsonUtil.getOptionalBoolean(obj, "normalized_uvs");
            double[] normals = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "normals", null));
            PolysUnion polysUnion = GsonHelper.getAsObject(obj, "polys", null, context, PolysUnion.class);
            double[] positions = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "positions", null));
            double[] uvs = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "uvs", null));

            return new PolyMesh(normalizedUVs, normals, polysUnion, positions, uvs);
        };
    }
}
