package mod.azure.azurelib.loading.json.raw;

import javax.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.JSONUtils;

/**
 * Container class for generic geometry information, only used in deserialization at startup
 */
public class MinecraftGeometry {
	public Bone[] bones;
	@Nullable
	public String cape;
	@Nullable
	public ModelProperties modelProperties;

	public MinecraftGeometry(Bone[] bones, @Nullable String cape, @Nullable ModelProperties modelProperties) {

	}

	public Bone[] bones() {
		return bones;
	}

	@Nullable
	public String cape() {
		return cape;
	}

	@Nullable
	public ModelProperties modelProperties() {
		return modelProperties;
	}

	public static JsonDeserializer<MinecraftGeometry> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Bone[] bones = JsonUtil.jsonArrayToObjectArray(JSONUtils.getAsJsonArray(obj, "bones", new JsonArray()), context, Bone.class);
			String cape = JSONUtils.getAsString(obj, "cape", null);
			ModelProperties modelProperties = JSONUtils.getAsObject(obj, "description", null, context, ModelProperties.class);

			return new MinecraftGeometry(bones, cape, modelProperties);
		};
	}
}
