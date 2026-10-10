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

import java.util.Map;

import mod.azure.azurelib.util.GsonHelper;
import mod.azure.azurelib.util.JsonUtil;

/**
 * Container class for cube information, only used in deserialization at startup
 */
public final class Bone {

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
        return this.bindPoseRotation;
    }

    public Cube[] cubes() {
        return this.cubes;
    }

    public Boolean debug() {
        return this.debug;
    }

    public Double inflate() {
        return this.inflate;
    }

    public Map<String, LocatorValue> locators() {
        return this.locators;
    }

    public Boolean mirror() {
        return this.mirror;
    }

    public String name() {
        return this.name;
    }

    public Boolean neverRender() {
        return this.neverRender;
    }

    public String parent() {
        return this.parent;
    }

    public double[] pivot() {
        return this.pivot;
    }

    public PolyMesh polyMesh() {
        return this.polyMesh;
    }

    public Long renderGroupId() {
        return this.renderGroupId;
    }

    public Boolean reset() {
        return this.reset;
    }

    public double[] rotation() {
        return this.rotation;
    }

    public TextureMesh[] textureMeshes() {
        return this.textureMeshes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Bone))
            return false;
        Bone other = (Bone) o;
        return java.util.Objects.equals(this.bindPoseRotation, other.bindPoseRotation)
            && java.util.Objects.equals(this.cubes, other.cubes)
            && java.util.Objects.equals(this.debug, other.debug)
            && java.util.Objects.equals(this.inflate, other.inflate)
            && java.util.Objects.equals(this.locators, other.locators)
            && java.util.Objects.equals(this.mirror, other.mirror)
            && java.util.Objects.equals(this.name, other.name)
            && java.util.Objects.equals(this.neverRender, other.neverRender)
            && java.util.Objects.equals(this.parent, other.parent)
            && java.util.Objects.equals(this.pivot, other.pivot)
            && java.util.Objects.equals(this.polyMesh, other.polyMesh)
            && java.util.Objects.equals(this.renderGroupId, other.renderGroupId)
            && java.util.Objects.equals(this.reset, other.reset)
            && java.util.Objects.equals(this.rotation, other.rotation)
            && java.util.Objects.equals(this.textureMeshes, other.textureMeshes);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.bindPoseRotation);
        result = 31 * result + java.util.Objects.hashCode(this.cubes);
        result = 31 * result + java.util.Objects.hashCode(this.debug);
        result = 31 * result + java.util.Objects.hashCode(this.inflate);
        result = 31 * result + java.util.Objects.hashCode(this.locators);
        result = 31 * result + java.util.Objects.hashCode(this.mirror);
        result = 31 * result + java.util.Objects.hashCode(this.name);
        result = 31 * result + java.util.Objects.hashCode(this.neverRender);
        result = 31 * result + java.util.Objects.hashCode(this.parent);
        result = 31 * result + java.util.Objects.hashCode(this.pivot);
        result = 31 * result + java.util.Objects.hashCode(this.polyMesh);
        result = 31 * result + java.util.Objects.hashCode(this.renderGroupId);
        result = 31 * result + java.util.Objects.hashCode(this.reset);
        result = 31 * result + java.util.Objects.hashCode(this.rotation);
        result = 31 * result + java.util.Objects.hashCode(this.textureMeshes);
        return result;
    }

    @Override
    public String toString() {
        return "Bone[bindPoseRotation=" + this.bindPoseRotation + ", cubes=" + this.cubes + ", debug=" + this.debug
            + ", inflate=" + this.inflate + ", locators=" + this.locators + ", mirror=" + this.mirror + ", name="
            + this.name + ", neverRender=" + this.neverRender + ", parent=" + this.parent + ", pivot=" + this.pivot
            + ", polyMesh=" + this.polyMesh + ", renderGroupId=" + this.renderGroupId + ", reset=" + this.reset
            + ", rotation=" + this.rotation + ", textureMeshes=" + this.textureMeshes + "]";
    }

    public static JsonDeserializer<Bone> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            JsonObject obj = json.getAsJsonObject();
            double[] bindPoseRotation = JsonUtil.jsonArrayToDoubleArray(
                GsonHelper.getAsJsonArray(obj, "bind_pose_rotation", null)
            );
            Cube[] cubes = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "cubes", new JsonArray()),
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
            double[] pivot = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "pivot", new JsonArray()));
            PolyMesh polyMesh = GsonHelper.getAsObject(obj, "poly_mesh", null, context, PolyMesh.class);
            Long renderGroupId = JsonUtil.getOptionalLong(obj, "render_group_id");
            Boolean reset = JsonUtil.getOptionalBoolean(obj, "reset");
            double[] rotation = JsonUtil.jsonArrayToDoubleArray(GsonHelper.getAsJsonArray(obj, "rotation", null));
            TextureMesh[] textureMeshes = JsonUtil.jsonArrayToObjectArray(
                GsonHelper.getAsJsonArray(obj, "texture_meshes", new JsonArray()),
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
