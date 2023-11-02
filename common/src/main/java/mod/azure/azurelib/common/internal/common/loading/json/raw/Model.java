package mod.azure.azurelib.common.internal.common.loading.json.raw;

import mod.azure.azurelib.common.internal.common.loading.json.FormatVersion;
import mod.azure.azurelib.common.internal.common.util.JsonUtil;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.GsonHelper;

/**
 * Container class for model information, only used in deserialization at startup
 */
public record Model(@Nullable FormatVersion formatVersion, MinecraftGeometry[] minecraftGeometry) {
	public static JsonDeserializer<Model> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			FormatVersion formatVersion = context.deserialize(obj.get("format_version"), FormatVersion.class);
			MinecraftGeometry[] minecraftGeometry = JsonUtil.jsonArrayToObjectArray(GsonHelper.getAsJsonArray(obj, "minecraft:geometry", new JsonArray(0)), context, MinecraftGeometry.class);

			return new Model(formatVersion, minecraftGeometry);
		};
	}
}
