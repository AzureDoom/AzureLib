/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.util.JSONUtils;

import java.util.Map;

import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public class Bone {

    private final double[] bindPoseRotation;

    private final Cube[] cubes;

    private final Boolean debug;

    private final Double inflate;

    private final Map<String, LocatorValue> locators;

    private final Boolean mirror;

    private final String name;

    private final Boolean neverRender;

    private final String parent;

    private final double[] pivot;

    private final PolyMesh polyMesh;

    private final Long renderGroupId;

    private final Boolean reset;

    private final double[] rotation;

    private final TextureMesh[] textureMeshes;

    public Bone(
        double[] bindPoseRotation,
        Cube[] cubes,
        Boolean debug,
        Double inflate,
        Map<String, LocatorValue> locators,
        Boolean mirror,
        String name,
        Boolean neverRender,
        String parent,
        double[] pivot,
        PolyMesh polyMesh,
        Long renderGroupId,
        Boolean reset,
        double[] rotation,
        TextureMesh[] textureMeshes
    ) {
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

    public Boolean debug() {
        return debug;
    }

    public Double inflate() {
        return inflate;
    }

    public Map<String, LocatorValue> locators() {
        return locators;
    }

    public Boolean mirror() {
        return mirror;
    }

    public String name() {
        return name;
    }

    public Boolean neverRender() {
        return neverRender;
    }

    public String parent() {
        return parent;
    }

    public double[] pivot() {
        return pivot;
    }

    public PolyMesh polyMesh() {
        return polyMesh;
    }

    public Long renderGroupId() {
        return renderGroupId;
    }

    public Boolean reset() {
        return reset;
    }

    public double[] rotation() {
        return rotation;
    }

    public TextureMesh[] textureMeshes() {
        return textureMeshes;
    }

    public static JsonDeserializer<Bone> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            double[] bindPoseRotation = JsonUtil.jsonArrayToDoubleArray(
                JSONUtils.getAsJsonArray(obj, "bind_pose_rotation", null)
            );
            Cube[] cubes = JsonUtil.jsonArrayToObjectArray(
                JSONUtils.getAsJsonArray(obj, "cubes", new JsonArray()),
                context,
                Cube.class
            );
            Boolean debug = JsonUtil.getOptionalBoolean(obj, "debug");
            Double inflate = JsonUtil.getOptionalDouble(obj, "inflate");
            Map<String, LocatorValue> locators = obj.has("locators")
                ? JsonUtil.jsonObjToMap(JSONUtils.getAsJsonObject(obj, "locators"), context, LocatorValue.class)
                : null;
            Boolean mirror = JsonUtil.getOptionalBoolean(obj, "mirror");
            String name = JSONUtils.getAsString(obj, "name", null);
            Boolean neverRender = JsonUtil.getOptionalBoolean(obj, "neverRender");
            String parent = JSONUtils.getAsString(obj, "parent", null);
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "pivot", new JsonArray()));
            PolyMesh polyMesh = JSONUtils.getAsObject(obj, "poly_mesh", null, context, PolyMesh.class);
            Long renderGroupId = JsonUtil.getOptionalLong(obj, "render_group_id");
            Boolean reset = JsonUtil.getOptionalBoolean(obj, "reset");
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(JSONUtils.getAsJsonArray(obj, "rotation", null));
            TextureMesh[] textureMeshes = JsonUtil.jsonArrayToObjectArray(
                JSONUtils.getAsJsonArray(obj, "texture_meshes", new JsonArray()),
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
