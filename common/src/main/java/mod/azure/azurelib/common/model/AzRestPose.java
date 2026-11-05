package mod.azure.azurelib.common.model;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Collections;
import java.util.Map;

import mod.azure.azurelib.common.loading.json.raw.Bone;
import mod.azure.azurelib.common.loading.object.BoneStructure;
import mod.azure.azurelib.common.loading.object.GeometryTree;

/**
 * The model-space rest (bind) transform of every bone in a geometry file, built from the raw geometry data rather than
 * from live {@link AzBone} state.
 * <p>
 * Skinned meshes measure each joint's movement against this pose. Taking it straight from the file means it can't be
 * affected by whatever pose a bone happens to be in when a model is built or copied. Matrices follow
 * {@link AzSkinning#mulBoneLocal} with zero position offset and unit scale.
 * </p>
 */
public final class AzRestPose {

    public static final AzRestPose EMPTY = new AzRestPose(Map.of());

    private final Map<String, Matrix4fc> modelMatrices;

    private AzRestPose(Map<String, Matrix4fc> modelMatrices) {
        this.modelMatrices = modelMatrices;
    }

    public static AzRestPose of(GeometryTree geometryTree) {
        Object2ObjectOpenHashMap<String, Matrix4fc> matrices = new Object2ObjectOpenHashMap<>();

        for (BoneStructure structure : geometryTree.topLevelBones().values()) {
            collect(structure, new Matrix4f(), matrices);
        }

        return new AzRestPose(Collections.unmodifiableMap(matrices));
    }

    private static void collect(BoneStructure structure, Matrix4fc parent, Map<String, Matrix4fc> out) {
        Bone bone = structure.self();
        Matrix4f matrix = mulRestLocal(new Matrix4f(parent), bone);

        if (bone.name() != null) {
            out.put(bone.name(), matrix);
        }

        for (BoneStructure child : structure.children().values()) {
            collect(child, matrix, out);
        }
    }

    public static Matrix4f mulRestLocal(Matrix4f matrix, Bone bone) {
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

    private static double component(double @Nullable [] vector, int index) {
        return vector != null && vector.length > index ? vector[index] : 0d;
    }

    public @Nullable Matrix4fc modelMatrix(String boneName) {
        return modelMatrices.get(boneName);
    }

    public boolean contains(String boneName) {
        return modelMatrices.containsKey(boneName);
    }
}
