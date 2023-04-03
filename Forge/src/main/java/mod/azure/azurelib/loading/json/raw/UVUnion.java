package mod.azure.azurelib.loading.json.raw;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for UV information, only used in deserialization at startup
 */
public class UVUnion {

	public double[] boxUVCoords;
	@Nullable
	public UVFaces faceUV;

	public boolean isBoxUV;

	public UVUnion(double[] boxUVCoords, @Nullable UVFaces faceUV, boolean isBoxUV) {
		this.boxUVCoords = boxUVCoords;
		this.faceUV = faceUV;
		this.isBoxUV = isBoxUV;
	}

	public double[] boxUVCoords() {
		return boxUVCoords;
	}

	@Nullable
	public UVFaces faceUV() {
		return faceUV;
	}

	public boolean isBoxUV() {
		return isBoxUV;
	}

	public static JsonDeserializer<UVUnion> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			if (json.isJsonObject()) {
				return new UVUnion(new double[0], context.deserialize(json.getAsJsonObject(), UVFaces.class), false);
			} else if (json.isJsonArray()) {
				return new UVUnion(JsonUtil.jsonArrayToDoubleArray(json.getAsJsonArray()), null, true);
			} else {
				throw new JsonParseException("Invalid format provided for UVUnion, must be either double array or UVFaces collection");
			}
		};
	}
}
