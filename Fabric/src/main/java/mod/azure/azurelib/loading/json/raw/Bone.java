package mod.azure.azurelib.loading.json.raw;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import mod.azure.azurelib.util.JsonUtil;
import net.minecraft.util.GsonHelper;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public class Bone {
	public double[] bindPoseRotation;
	public Cube[] cubes;
	@Nullable
	public Boolean debug;
	@Nullable
	public Double inflate;
	@Nullable
	public Map<String, LocatorValue> locators;
	@Nullable
	public Boolean mirror;
	@Nullable
	public String name;
	@Nullable
	public Boolean neverRender;
	@Nullable
	public String parent;
	public double[] pivot;
	@Nullable
	public PolyMesh polyMesh;
	@Nullable
	public Long renderGroupId;
	@Nullable
	public Boolean reset;
	public double[] rotation;
	@Nullable
	public TextureMesh[] textureMeshes;

	public Bone(double[] bindPoseRotation, Cube[] cubes, @Nullable Boolean debug, @Nullable Double inflate, @Nullable Map<String, LocatorValue> locators, @Nullable Boolean mirror, @Nullable String name, @Nullable Boolean neverRender, @Nullable String parent, double[] pivot, @Nullable PolyMesh polyMesh, @Nullable Long renderGroupId, @Nullable Boolean reset, double[] rotation, @Nullable TextureMesh[] textureMeshes) {
		this.bindPoseRotation = bindPoseRotation;
		this.cubes = cubes;
		this.debug = debug;
		this.inflate = inflate;
		this.locators = locators;
		this.mirror = mirror;
		this.name = name;
		this.neverRender = neverRender;
		this.parent = parent;
		this.pivot = pivot;
		this.polyMesh = polyMesh;
		this.renderGroupId = renderGroupId;
		this.reset = reset;
		this.rotation = rotation;
		this.textureMeshes = textureMeshes;
	}

	public double[] bindPoseRotation() {
		return bindPoseRotation;
	}

	public Cube[] cubes() {
		return cubes;
	}

	@Nullable
	public Boolean debug() {
		return debug;
	}

	@Nullable
	public Double inflate() {
		return inflate;
	}

	@Nullable
	public Map<String, LocatorValue> locators() {
		return locators;
	}

	@Nullable
	public Boolean mirror() {
		return mirror;
	}

	@Nullable
	public String name() {
		return name;
	}

	@Nullable
	public Boolean neverRender() {
		return neverRender;
	}

	@Nullable
	public String parent() {
		return parent;
	}

	public double[] pivot() {
		return pivot;
	}

	@Nullable
	public PolyMesh polyMesh() {
		return polyMesh;
	}

	@Nullable
	public Long renderGroupId() {
		return renderGroupId;
	}

	@Nullable
	public Boolean reset() {
		return reset;
	}

	public double[] rotation() {
		return rotation;
	}

	@Nullable
	public TextureMesh[] textureMeshes() {
		return textureMeshes;
	}

	public static JsonDeserializer<Bone> deserializer() throws JsonParseException {
		return (json, type, context) -> {
			JsonObject obj = json.getAsJsonObject();
			double[] bindPoseRotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "bind_pose_rotation", null));
			Cube[] cubes = JsonUtil.jsonArrayToObjectArray(GsonHelper.getAsJsonArray(obj, "cubes", new JsonArray()), context, Cube.class);
			Boolean debug = JsonUtil.getOptionalBoolean(obj, "debug");
			Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
			Map<String, LocatorValue> locators = obj.has("locators") ? JsonUtil.jsonObjToMap(GsonHelper.getAsJsonObject(obj, "locators"), context, LocatorValue.class) : null;
			Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
			String name = GsonHelper.getAsString(obj, "name", null);
			Boolean neverRender = JsonUtil.getOptionalBoolean(obj, "neverRender");
			String parent = GsonHelper.getAsString(obj, "parent", null);
			double[] pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "pivot", new JsonArray()));
			PolyMesh polyMesh = GsonHelper.getAsObject(obj, "poly_mesh", null, context, PolyMesh.class);
			Long renderGroupId = JsonUtil.getOptionalLong(obj, "render_group_id");
			Boolean reset = JsonUtil.getOptionalBoolean(obj, "reset");
			double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
			TextureMesh[] textureMeshes = JsonUtil.jsonArrayToObjectArray(GsonHelper.getAsJsonArray(obj, "texture_meshes", new JsonArray()), context, TextureMesh.class);

			return new Bone(bindPoseRotation, cubes, debug, inflate, locators, mirror, name, neverRender, parent, pivot, polyMesh, renderGroupId, reset, rotation, textureMeshes);
		};
	}
}
