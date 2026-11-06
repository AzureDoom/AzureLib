package mod.azure.azurelib.loading.json.raw;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Container class for an entry of a bone's {@code azurelib:skinned_meshes} list, written by the AzureLib Blockbench
 * plugin for meshes inside an armature. Only used in deserialization at startup.
 * <p>
 * The mesh fields match {@link PolyMesh}, except that positions are in model space at rest pose instead of bone space.
 * {@code weights} holds, per position, a list of {@code [jointIndex, weight]} pairs indexing into {@code joints}.
 * </p>
 */
public final class SkinnedMesh {

    public static final String KEY = "azurelib:skinned_meshes";

    private final String name;

    private final String[] joints;

    private final PolyMesh mesh;

    private final int[][] influenceJoints;

    private final float[][] influenceWeights;

    /**
     * @param name             the mesh's name in Blockbench, for diagnostics
     * @param joints           the names of the bones this mesh is weighted to
     * @param mesh             the polygon data
     * @param influenceJoints  per position, the joint indices that influence it
     * @param influenceWeights per position, the matching weights
     */
    public SkinnedMesh(
        String name,
        String[] joints,
        PolyMesh mesh,
        int[][] influenceJoints,
        float[][] influenceWeights
    ) {
        this.name = name;
        this.joints = joints;
        this.mesh = mesh;
        this.influenceJoints = influenceJoints;
        this.influenceWeights = influenceWeights;
    }

    public String name() {
        return name;
    }

    public String[] joints() {
        return joints;
    }

    public PolyMesh mesh() {
        return mesh;
    }

    public int[][] influenceJoints() {
        return influenceJoints;
    }

    public float[][] influenceWeights() {
        return influenceWeights;
    }

    public static JsonDeserializer<SkinnedMesh> deserializer() throws JsonParseException {
        return (json, type, context) -> {
            if (!json.isJsonObject()) {
                throw new JsonParseException("skinned mesh entries must be objects");
            }

            JsonObject obj = json.getAsJsonObject();
            JsonElement nameElement = obj.get("name");
            String name = nameElement != null && nameElement.isJsonPrimitive() ? nameElement.getAsString() : "mesh";

            try {
                return read(obj, name, context);
            } catch (JsonParseException e) {
                throw new JsonParseException("skinned mesh '" + name + "': " + e.getMessage(), e);
            }
        };
    }

    private static SkinnedMesh read(JsonObject obj, String name, JsonDeserializationContext context) {
        JsonArray jointArray = MeshValidation.array(obj.get("joints"), "joints");

        if (jointArray.size() == 0) {
            throw MeshValidation.error("joints", "must list at least one bone");
        }

        MeshValidation.checkCount("joints", jointArray.size(), MeshValidation.MAX_JOINTS);
        String[] joints = new String[jointArray.size()];

        for (int i = 0; i < joints.length; i++) {
            JsonElement joint = jointArray.get(i);

            if (!joint.isJsonPrimitive() || !joint.getAsJsonPrimitive().isString()) {
                throw MeshValidation.error("joints[" + i + "]", "must be a bone name");
            }

            joints[i] = joint.getAsString();
        }

        PolyMesh mesh = PolyMesh.read(obj, context);
        int positionCount = mesh.positions().length;

        if (!obj.has("weights")) {
            // No weights at all: every vertex follows the first joint (the armature).
            return new SkinnedMesh(name, joints, mesh, new int[positionCount][0], new float[positionCount][0]);
        }

        JsonArray weightArray = MeshValidation.array(obj.get("weights"), "weights");

        if (weightArray.size() != positionCount) {
            throw MeshValidation.error(
                "weights",
                "needs one entry per position: got " + weightArray.size() + " for " + positionCount + " positions"
            );
        }

        int[][] influenceJoints = new int[positionCount][];
        float[][] influenceWeights = new float[positionCount][];

        for (int p = 0; p < positionCount; p++) {
            String at = "weights[" + p + "]";
            JsonArray pairs = MeshValidation.array(weightArray.get(p), at);
            MeshValidation.checkCount(at, pairs.size(), MeshValidation.MAX_LISTED_INFLUENCES);
            influenceJoints[p] = new int[pairs.size()];
            influenceWeights[p] = new float[pairs.size()];

            for (int i = 0; i < pairs.size(); i++) {
                String pairAt = at + "[" + i + "]";
                JsonArray pair = MeshValidation.array(pairs.get(i), pairAt);

                if (pair.size() < 1 || pair.size() > 2) {
                    throw MeshValidation.error(pairAt, "must be [jointIndex] or [jointIndex, weight]");
                }

                int joint = MeshValidation.index(pair.get(0), pairAt);
                MeshValidation.checkIndex(joint, joints.length, pairAt, "joint");
                double weight = pair.size() > 1 ? MeshValidation.finiteFloat(pair.get(1), pairAt) : 1d;

                if (weight < 0) {
                    throw MeshValidation.error(pairAt, "weight must not be negative, got " + weight);
                }

                influenceJoints[p][i] = joint;
                influenceWeights[p][i] = (float) weight;
            }
        }

        return new SkinnedMesh(name, joints, mesh, influenceJoints, influenceWeights);
    }
}
