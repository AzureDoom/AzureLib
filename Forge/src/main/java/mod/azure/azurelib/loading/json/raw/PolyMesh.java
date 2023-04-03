package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;
import mod.azure.azurelib.util.JsonUtil;

import javax.annotation.Nullable;

/**
 * Container class for poly mesh information, only used in deserialization at startup
 */
public class PolyMesh {
	
	protected final @Nullable Boolean normalizedUVs;
	protected final double[] normals;
	protected final @Nullable PolysUnion polysUnion;
	protected final double[] positions;
	protected final double[] uvs;
	
	public PolyMesh(@Nullable Boolean normalizedUVs, double[] normals, @Nullable PolysUnion polysUnion, double[] positions, double[] uvs) {
		this.normalizedUVs = normalizedUVs;
		this.normals = normals;
		this.polysUnion = polysUnion;
		this.positions = positions;
		this.uvs = uvs;
	}
	
	public static JsonDeserializer<PolyMesh> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			Boolean normalizedUVs = JsonUtil.getOptionalBoolean(obj, "normalized_uvs");
			double[] normals = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "normals", null));
			PolysUnion polysUnion = JSONUtils.getAsObject(obj, "polys", null, context, PolysUnion.class);
			double[] positions = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "positions", null));
			double[] uvs = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "uvs", null));

			return new PolyMesh(normalizedUVs, normals, polysUnion, positions, uvs);
		};
	}
	
	@Nullable
	public Boolean normalizedUVs() {
		return this.normalizedUVs;
	}
	
	public double[] normals() {
		return this.normals;
	}
	
	@Nullable
	public PolysUnion polysUnion() {
		return this.polysUnion;
	}
	
	public double[] positions() {
		return this.positions;
	}
	
	public double[] uvs() {
		return this.uvs;
	}
	
}
