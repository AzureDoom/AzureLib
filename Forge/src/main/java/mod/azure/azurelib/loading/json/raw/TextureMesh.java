package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;
import mod.azure.azurelib.util.JsonUtil;

import javax.annotation.Nullable;

/**
 * Container class for texture mesh information, only used in deserialization at startup
 */
public class TextureMesh {
	
	protected final double[] localPivot;
	protected final double[] position;
	protected final double[] rotation;
	protected final double[] scale;
	protected final @Nullable String texture;
	
	public TextureMesh(double[] localPivot, double[] position, double[] rotation, double[] scale, @Nullable String texture) {
		this.localPivot = localPivot;
		this.position = position;
		this.rotation = rotation;
		this.scale = scale;
		this.texture = texture;
	}
	
	public static JsonDeserializer<TextureMesh> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			double[] pivot = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "local_pivot", null));
			double[] position = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "position", null));
			double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "rotation", null));
			double[] scale = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "scale", null));
			String texture = JSONUtils.getAsString(obj, "texture", null);

			return new TextureMesh(pivot, position, rotation, scale, texture);
		};
	}
	
	public double[] localPivot() {
		return this.localPivot;
	}
	
	public double[] position() {
		return this.position;
	}
	
	public double[] rotation() {
		return this.rotation;
	}
	
	public double[] scale() {
		return this.scale;
	}
	
	@Nullable
	public String texture() {
		return this.texture;
	}
	
}
