package mod.azure.azurelib.loading.json.raw;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.JSONUtils;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public class Cube {

	@Nullable
	public Double inflate;
	@Nullable
	public Boolean mirror;
	public double[] origin;
	public double[] pivot;
	public double[] rotation;
	public double[] size;
	public UVUnion uv;

	public Cube(@Nullable Double inflate, @Nullable Boolean mirror, double[] origin, double[] pivot, double[] rotation, double[] size, UVUnion uv) {
		this.inflate = inflate;
		this.mirror = mirror;
		this.origin = origin;
		this.pivot = pivot;
		this.rotation = rotation;
		this.size = size;
		this.uv = uv;
	}

	@Nullable
	public Double inflate() {
		return inflate;
	}

	@Nullable
	public Boolean mirror() {
		return mirror;
	}

	public double[] origin() {
		return origin;
	}

	public double[] pivot() {
		return pivot;
	}

	public double[] rotation() {
		return rotation;
	}

	public double[] size() {
		return size;
	}

	public UVUnion uv() {
		return uv;
	}

	public static JsonDeserializer<Cube> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
			Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
			double[] origin = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "origin", null));
			double[] pivot = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "pivot", null));
			double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "rotation", null));
			double[] size = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "size", null));
			UVUnion uvUnion = JSONUtils.getAsObject(obj, "uv", null, context, UVUnion.class);

			return new Cube(inflate, mirror, origin, pivot, rotation, size, uvUnion);
		};
	}
}
