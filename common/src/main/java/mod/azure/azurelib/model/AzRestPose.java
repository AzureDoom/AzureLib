package mod.azure.azurelib.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import mod.azure.azurelib.loading.json.raw.Bone;
import mod.azure.azurelib.loading.object.BoneStructure;
import mod.azure.azurelib.loading.object.GeometryTree;

/**
 * The model-space rest (bind) transform of every bone in a geometry file, built from the raw geometry data rather than
 * from live {@link AzBone} state.
 * <p>
 * Skinned meshes measure each joint's movement against this pose. Taking it straight from the file means it can't be
 * affected by whatever pose a bone happens to be in when a model is built or copied. Matrices follow
 * {@link AzSkinning#mulBoneLocal} with zero position offset and unit scale, as {@code float[16]} column-major arrays.
 * </p>
 */
public final class AzRestPose {

    public static final AzRestPose EMPTY = new AzRestPose(Collections.<String, float[]>emptyMap());

    private final Map<String, float[]> modelMatrices;

    private AzRestPose(Map<String, float[]> modelMatrices) {
        this.modelMatrices = modelMatrices;
    }

    public static AzRestPose of(GeometryTree geometryTree) {
        Map<String, float[]> matrices = new HashMap<>();

        for (BoneStructure structure : geometryTree.topLevelBones().values()) {
            collect(structure, AzSkinning.identity(new float[16]), matrices);
        }

        return new AzRestPose(Collections.unmodifiableMap(matrices));
    }

    private static void collect(BoneStructure structure, float[] parent, Map<String, float[]> out) {
        Bone bone = structure.self();
        float[] matrix = mulRestLocal(AzSkinning.set(new float[16], parent), bone);

        if (bone.name() != null) {
            out.put(bone.name(), matrix);
        }

        for (BoneStructure child : structure.children().values()) {
            collect(child, matrix, out);
        }
    }

    /**
     * Multiplies {@code matrix} by a raw bone's rest transform, converted the same way
     * {@code AzBuiltinBakedModelFactory#constructBone} converts pivot and rotation for {@link AzBone}.
     */
    public static float[] mulRestLocal(float[] matrix, Bone bone) {
        double[] pivot = bone.pivot();
        double[] rotation = bone.rotation();

        return AzSkinning.mulLocal(
            matrix,
            0f,
            0f,
            0f,
            (float) -component(pivot, 0),
            (float) component(pivot, 1),
            (float) component(pivot, 2),
            (float) Math.toRadians(-component(rotation, 0)),
            (float) Math.toRadians(-component(rotation, 1)),
            (float) Math.toRadians(component(rotation, 2)),
            1f,
            1f,
            1f
        );
    }

    private static double component(double[] vector, int index) {
        return vector != null && vector.length > index ? vector[index] : 0d;
    }

    /**
     * The bone's model-space rest transform, or null if the geometry has no bone with that name. The returned array is
     * shared: read it, don't change it.
     */
    public float[] modelMatrix(String boneName) {
        return modelMatrices.get(boneName);
    }

    public boolean contains(String boneName) {
        return modelMatrices.containsKey(boneName);
    }
}
