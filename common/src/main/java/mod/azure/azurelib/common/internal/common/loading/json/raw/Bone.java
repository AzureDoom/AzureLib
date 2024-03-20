package mod.azure.azurelib.common.internal.common.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import mod.azure.azurelib.common.internal.common.util.JsonUtil;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public record Bone(
    double[] bindPoseRotation,
    Cube[] cubes,
    @Nullable Boolean debug,
    @Nullable Double inflate,
    @Nullable Map<String, LocatorValue> locators,
    @Nullable Boolean mirror,
    @Nullable String name,
    @Nullable Boolean neverRender,
    @Nullable String parent,
    double[] pivot,
    @Nullable PolyMesh polyMesh,
    @Nullable Long renderGroupId,
    @Nullable Boolean reset,
    double[] rotation,
    @Nullable TextureMesh[] textureMeshes
) {

    public static JsonDeserializer<Bone> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            double[] bindPoseRotation = JsonUtil.jsonArrayToDoubleArray(
                GsonHelper.getAsJsonArray(obj, "bind_pose_rotation", null)
            );
            Cube[] cubes = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "cubes", new JsonArray(0)),
                context,
                Cube.class
            );
            Boolean debug = JsonUtil.getOptionalBoolean(obj, "debug");
            Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
            Map<String, LocatorValue> locators = obj.has("locators")
                ? JsonUtil.jsonObjToMap(GsonHelper.getAsJsonObject(obj, "locators"), context, LocatorValue.class)
                : null;
            Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
            String name = GsonHelper.getAsString(obj, "name", null);
            Boolean neverRender = JsonUtil.getOptionalBoolean(obj, "neverRender");
            String parent = GsonHelper.getAsString(obj, "parent", null);
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "pivot", new JsonArray(0)));
            PolyMesh polyMesh = GsonHelper.getAsObject(obj, "poly_mesh", null, context, PolyMesh.class);
            Long renderGroupId = JsonUtil.getOptionalLong(obj, "render_group_id");
            Boolean reset = JsonUtil.getOptionalBoolean(obj, "reset");
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
            TextureMesh[] textureMeshes = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "texture_meshes", new JsonArray(0)),
                context,
                TextureMesh.class
            );

            return new Bone(
                bindPoseRotation,
                cubes,
                debug,
                inflate,
                locators,
                mirror,
                name,
                neverRender,
                parent,
                pivot,
                polyMesh,
                renderGroupId,
                reset,
                rotation,
                textureMeshes
            );
        };
    }
}
