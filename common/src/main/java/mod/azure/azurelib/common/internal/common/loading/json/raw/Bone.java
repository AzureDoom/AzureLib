/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
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
            var obj = json.getAsJsonObject();
            var bindPoseRotation = JsonUtil.jsonArrayToDoubleArray(
                GsonHelper.getAsJsonArray(obj, "bind_pose_rotation", null)
            );
            var cubes = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "cubes", new JsonArray(0)),
                context,
                Cube.class
            );
            var debug = JsonUtil.getOptionalBoolean(obj, "debug");
            var inflate = JsonUtil.getOptionalDouble(obj, "inflate");
            var locators = obj.has("locators")
                ? JsonUtil.jsonObjToMap(GsonHelper.getAsJsonObject(obj, "locators"), context, LocatorValue.class)
                : null;
            var mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
            var name = GsonHelper.getAsString(obj, "name", null);
            var neverRender = JsonUtil.getOptionalBoolean(obj, "neverRender");
            var parent = GsonHelper.getAsString(obj, "parent", null);
            var pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "pivot", new JsonArray(0)));
            var polyMesh = GsonHelper.getAsObject(obj, "poly_mesh", null, context, PolyMesh.class);
            var renderGroupId = JsonUtil.getOptionalLong(obj, "render_group_id");
            var reset = JsonUtil.getOptionalBoolean(obj, "reset");
            var rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
            var textureMeshes = JsonUtil.jsonArrayToObjectArray(
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
