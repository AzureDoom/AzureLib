package mod.azure.azurelib.cache.object;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzSkinning;
import mod.azure.azurelib.model.factory.primitive.MeshPolygons;

/**
 * Baked skinned polygon mesh, deformed every frame by the bones in {@link #joint(int) its joints} with linear blend
 * skinning.
 * <p>
 * Rest positions are in model space. Each position has up to {@link #MAX_INFLUENCES} joint influences with weights that
 * sum to one. The mesh belongs to an owner {@link AzBone} (the Blockbench armature) and renders as part of it, so
 * hiding the owner hides the mesh.
 * </p>
 * <p>
 * Instances are immutable and shared between copies of a baked model: arrays are copied on construction, array
 * accessors return copies, and per-frame code reads through the indexed getters, which don't allocate. The inverse bind
 * matrices come from the geometry file's rest pose ({@link mod.azure.azurelib.model.AzRestPose}), never from live bone
 * state: the factory builds an unbound mesh, then {@link #withBindPose} produces the bound one once the whole bone tree
 * is known. An unbound mesh does not render.
 * </p>
 * <p>
 * Matrices are {@code float[16]}, column-major, as used by {@link AzSkinning}.
 * </p>
 */
public final class GeoSkinnedMesh {

    public static final int MAX_INFLUENCES = 4;

    private final String name;

    private final String[] joints;

    private final MeshPolygons polygons;

    private final int[] influenceJoints;

    private final float[] influenceWeights;

    private final float[][] inverseBindMatrices;

    public GeoSkinnedMesh(
        String name,
        String[] joints,
        MeshPolygons polygons,
        int[] influenceJoints,
        float[] influenceWeights
    ) {
        this(name, joints, polygons, influenceJoints, influenceWeights, null);
    }

    private GeoSkinnedMesh(
        String name,
        String[] joints,
        MeshPolygons polygons,
        int[] influenceJoints,
        float[] influenceWeights,
        float[][] inverseBindMatrices
    ) {
        // Defensive copies: instances are shared by every copy of the model, so callers must not be able to change
        // them through the arrays they passed in. MeshPolygons copies its own arrays.
        this.name = name;
        this.joints = joints.clone();
        this.polygons = polygons;
        this.influenceJoints = influenceJoints.clone();
        this.influenceWeights = influenceWeights.clone();
        this.inverseBindMatrices = inverseBindMatrices;
    }

    public String name() {
        return name;
    }

    /** A copy of the joint names. Use {@link #joint} and {@link #jointCount} on hot paths. */
    public String[] joints() {
        return joints.clone();
    }

    public int jointCount() {
        return joints.length;
    }

    public String joint(int index) {
        return joints[index];
    }

    public MeshPolygons polygons() {
        return polygons;
    }

    public int positionCount() {
        return polygons.positionCount();
    }

    /** A copy of the influence joint indices. Use {@link #influenceJoint} on hot paths. */
    public int[] influenceJoints() {
        return influenceJoints.clone();
    }

    /** A copy of the influence weights. Use {@link #influenceWeight} on hot paths. */
    public float[] influenceWeights() {
        return influenceWeights.clone();
    }

    /** Joint index of influence {@code i} ({@code position * MAX_INFLUENCES + slot}). */
    public int influenceJoint(int i) {
        return influenceJoints[i];
    }

    /** Weight of influence {@code i} ({@code position * MAX_INFLUENCES + slot}); zero for unused slots. */
    public float influenceWeight(int i) {
        return influenceWeights[i];
    }

    public boolean isBound() {
        return inverseBindMatrices != null;
    }

    /**
     * {@code matrix = matrix * inverseBind(joint)}, using {@code scratch} (16 floats) as working space.
     *
     * @throws IllegalStateException if the mesh is not bound
     */
    public float[] mulInverseBind(int joint, float[] matrix, float[] scratch) {
        if (inverseBindMatrices == null) {
            throw new IllegalStateException("Skinned mesh '" + name + "' is not bound");
        }

        return AzSkinning.mul(matrix, inverseBindMatrices[joint], matrix, scratch);
    }

    /**
     * Returns a bound copy of this mesh.
     *
     * @param restJointMatrices model-space rest transform of each joint, parallel to {@link #joint(int)}
     */
    public GeoSkinnedMesh withBindPose(float[][] restJointMatrices) {
        if (restJointMatrices.length != joints.length) {
            throw new IllegalArgumentException(
                "Skinned mesh '" + name + "' has " + joints.length + " joints but " + restJointMatrices.length
                    + " rest matrices were given"
            );
        }

        float[][] inverse = new float[restJointMatrices.length][];

        for (int i = 0; i < restJointMatrices.length; i++) {
            inverse[i] = new float[16];
            AzSkinning.invert(restJointMatrices[i], inverse[i]);
        }

        return new GeoSkinnedMesh(name, joints, polygons, influenceJoints, influenceWeights, inverse);
    }

    public boolean isEmpty() {
        return polygons.quadCount() == 0;
    }
}
