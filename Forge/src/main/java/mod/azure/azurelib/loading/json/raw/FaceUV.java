package mod.azure.azurelib.loading.json.raw;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.JSONUtils;

/**
 * Container class for face UV information, only used in deserialization at startup
 */
public class FaceUV {
	@Nullable
	public String materialInstance;
	public double[] uv;
	public double[] uvSize;

	public FaceUV(@Nullable String materialInstance, double[] uv, double[] uvSize) {
		this.materialInstance = materialInstance;
		this.uv = uv;
		this.uvSize = uvSize;
	}

	@Nullable
	public String materialInstance() {
		return materialInstance;
	}

	public double[] uv() {
		return uv;
	}

	public double[] uvSize() {
		return uvSize;
	}

	public static JsonDeserializer<FaceUV> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			String materialInstance = JSONUtils.getAsString(obj, "material_instance", null);
			double[] uv = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uv", null));
			double[] uvSize = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uv_size", null));

			return new FaceUV(materialInstance, uv, uvSize);
		};
	}
}
