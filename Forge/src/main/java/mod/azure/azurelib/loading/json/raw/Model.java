package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import mod.azure.azurelib.loading.json.FormatVersion;
import mod.azure.azurelib.util.JsonUtil;

import javax.annotation.Nullable;

/**
 * Container class for model information, only used in deserialization at startup
 */
public record Model(@Nullable FormatVersion formatVersion, MinecraftGeometry[] minecraftGeometry) {
	public static JsonDeserializer<Model> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			FormatVersion formatVersion = context.deserialize(obj.get("format_version"), FormatVersion.class);
			MinecraftGeometry[] minecraftGeometry = JsonUtil.jsonArrayToObjectArray(JSONUtils.getAsJsonArray(obj, "minecraft:geometry", new JsonArray()), context, MinecraftGeometry.class);

			return new Model(formatVersion, minecraftGeometry);
		};
	}
}
