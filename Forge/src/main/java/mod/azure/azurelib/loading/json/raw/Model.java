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
import net.minecraft.util.JSONUtils;

import mod.azure.azurelib.loading.json.FormatVersion;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for model information, only used in deserialization at startup
 */
public class Model {

    private final FormatVersion formatVersion;

    private final MinecraftGeometry[] minecraftGeometry;

    public Model(FormatVersion formatVersion, MinecraftGeometry[] minecraftGeometry) {
        this.formatVersion = formatVersion;
        this.minecraftGeometry = minecraftGeometry;
    }

    public FormatVersion formatVersion() {
        return formatVersion;
    }

    public MinecraftGeometry[] minecraftGeometry() {
        return minecraftGeometry;
    }

    public static JsonDeserializer<Model> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            FormatVersion formatVersion = context.deserialize(obj.get("format_version"), FormatVersion.class);
            MinecraftGeometry[] minecraftGeometry = JsonUtil.jsonArrayToObjectArray(
                JSONUtils.getAsJsonArray(obj, "minecraft:geometry", new JsonArray()),
                context,
                MinecraftGeometry.class
            );

            return new Model(formatVersion, minecraftGeometry);
        };
    }
}
