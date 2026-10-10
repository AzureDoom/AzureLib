/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for generic geometry information, only used in deserialization at startup
 */
public final class MinecraftGeometry {

    private final Bone[] bones;

    private final String cape;

    private final ModelProperties modelProperties;

    public MinecraftGeometry(Bone[] bones, String cape, ModelProperties modelProperties) {
        this.bones = bones;
        this.cape = cape;
        this.modelProperties = modelProperties;
    }

    public Bone[] bones() {
        return this.bones;
    }

    public String cape() {
        return this.cape;
    }

    public ModelProperties modelProperties() {
        return this.modelProperties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof MinecraftGeometry))
            return false;
        MinecraftGeometry other = (MinecraftGeometry) o;
        return java.util.Objects.equals(this.bones, other.bones)
            && java.util.Objects.equals(this.cape, other.cape)
            && java.util.Objects.equals(this.modelProperties, other.modelProperties);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.bones);
        result = 31 * result + java.util.Objects.hashCode(this.cape);
        result = 31 * result + java.util.Objects.hashCode(this.modelProperties);
        return result;
    }

    @Override
    public String toString() {
        return "MinecraftGeometry[bones=" + this.bones + ", cape=" + this.cape + ", modelProperties="
            + this.modelProperties + "]";
    }

    public static JsonDeserializer<MinecraftGeometry> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            Bone[] bones = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "bones", new JsonArray()),
                context,
                Bone.class
            );
            String cape = GsonHelper.getAsString(obj, "cape", null);
            ModelProperties modelProperties = GsonHelper.getAsObject(
                obj,
                "description",
                null,
                context,
                ModelProperties.class
            );

            return new MinecraftGeometry(bones, cape, modelProperties);
        };
    }
}
