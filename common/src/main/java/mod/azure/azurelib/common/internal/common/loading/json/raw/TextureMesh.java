package mod.azure.azurelib.common.internal.common.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.util.JsonUtil;

/**
 * Container class for texture mesh information, only used in deserialization at startup
 */
public record TextureMesh(
    double[] localPivot,
    double[] position,
    double[] rotation,
    double[] scale,
    @Nullable String texture
) {

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
