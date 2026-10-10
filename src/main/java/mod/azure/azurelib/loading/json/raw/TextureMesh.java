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
 * Container class for texture mesh information, only used in deserialization at startup
 */
public final class TextureMesh {

    private final double[] localPivot;

    private final double[] position;

    private final double[] rotation;

    private final double[] scale;

    private final String texture;

    public TextureMesh(double[] localPivot, double[] position, double[] rotation, double[] scale, String texture) {
        this.localPivot = localPivot;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.texture = texture;
    }

    public double[] localPivot() {
        return this.localPivot;
    }

    public double[] position() {
        return this.position;
    }

    public double[] rotation() {
        return this.rotation;
    }

    public double[] scale() {
        return this.scale;
    }

    public String texture() {
        return this.texture;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TextureMesh))
            return false;
        TextureMesh other = (TextureMesh) o;
        return java.util.Objects.equals(this.localPivot, other.localPivot)
            && java.util.Objects.equals(this.position, other.position)
            && java.util.Objects.equals(this.rotation, other.rotation)
            && java.util.Objects.equals(this.scale, other.scale)
            && java.util.Objects.equals(this.texture, other.texture);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.localPivot);
        result = 31 * result + java.util.Objects.hashCode(this.position);
        result = 31 * result + java.util.Objects.hashCode(this.rotation);
        result = 31 * result + java.util.Objects.hashCode(this.scale);
        result = 31 * result + java.util.Objects.hashCode(this.texture);
        return result;
    }

    @Override
    public String toString() {
        return "TextureMesh[localPivot=" + this.localPivot + ", position=" + this.position + ", rotation="
            + this.rotation + ", scale=" + this.scale + ", texture=" + this.texture + "]";
    }

    public static JsonDeserializer<TextureMesh> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "local_pivot", null));
            double[] position = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "position", null));
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
            double[] scale = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "scale", null));
            String texture = GsonHelper.getAsString(obj, "texture", null);

            return new TextureMesh(pivot, position, rotation, scale, texture);
        };
    }
}
