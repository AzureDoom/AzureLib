package mod.azure.azurelib.common.internal.common.loading.json.raw;

import mod.azure.azurelib.common.internal.common.util.JsonUtil;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

/**
 * Container class for UV information, only used in deserialization at startup
 */
public record UVUnion(double[] boxUVCoords, @Nullable UVFaces faceUV, boolean isBoxUV) {
	public static JsonDeserializer<UVUnion> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			if (json.isJsonObject()) {
				return new UVUnion(new double[0], context.deserialize(json.getAsJsonObject(), UVFaces.class), false);
			}
			else if (json.isJsonArray()) {
				return new UVUnion(JsonUtil.jsonArrayToDoubleArray(json.getAsJsonArray()), null, true);
			}
			else {
				throw new JsonParseException("Invalid format provided for UVUnion, must be either double array or UVFaces collection");
			}
		};
	}
}
