package mod.azure.azurelib.common.internal.common.loading.json.raw;

import mod.azure.azurelib.common.internal.common.util.JsonUtil;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.GsonHelper;

/**
 * Container class for face UV information, only used in deserialization at startup
 */
public record FaceUV(@Nullable String materialInstance, double[] uv, double[] uvSize) {
	public static JsonDeserializer<FaceUV> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			String materialInstance = GsonHelper.getAsString(obj, "material_instance", null);
			double[] uv = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "uv", null));
			double[] uvSize = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "uv_size", null));

			return new FaceUV(materialInstance, uv, uvSize);
		};
	}
}
