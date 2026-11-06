package mod.azure.azurelib.cache.object;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzRestPose;
import mod.azure.azurelib.model.factory.primitive.MeshPolygons;

/**
 * Baked skinned polygon mesh, deformed every frame by the bones in {@link #joints()} with linear blend skinning.
 * <p>
 * Rest positions are in model space. Each position has up to {@link #MAX_INFLUENCES} joint influences with weights that
 * sum to one. The mesh belongs to an owner {@link AzBone} (the Blockbench armature) and renders as part of it, so
 * hiding the owner hides the mesh.
 * </p>
 * <p>
 * Instances are immutable and shared between copies of a baked model: arrays are copied on construction, array
 * accessors return copies, and per-frame code reads through the indexed getters, which don't allocate. The inverse bind
 * matrices come from the geometry file's rest pose ({@link AzRestPose}), never from live bone state: the factory builds
 * an unbound mesh, then {@link #withBindPose} produces the bound one once the whole bone tree is known. An unbound mesh
 * does not render.
 * </p>
 */
public final class GeoSkinnedMesh {

    public static final int MAX_INFLUENCES = 4;

    private final String name;

    private final String[] joints;

    private final MeshPolygons polygons;

    private final int[] influenceJoints;

    private final float[] influenceWeights;

    private final Matrix4fc @Nullable [] inverseBindMatrices;

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
        Matrix4fc @Nullable [] inverseBindMatrices
    ) {
        this.inverseBindMatrices = inverseBindMatrices;
        this.name = name;
        this.joints = joints.clone();
        this.polygons = polygons;
        this.influenceJoints = influenceJoints.clone();
        this.influenceWeights = influenceWeights.clone();
    }

    public String name() {
        return name;
    }

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

    public int[] influenceJoints() {
        return influenceJoints.clone();
    }

    public float[] influenceWeights() {
        return influenceWeights.clone();
    }

    public int influenceJoint(int i) {
        return influenceJoints[i];
    }

    public float influenceWeight(int i) {
        return influenceWeights[i];
    }

    public boolean isBound() {
        return inverseBindMatrices != null;
    }

    public Matrix4fc @Nullable [] inverseBindMatrices() {
        return inverseBindMatrices != null ? inverseBindMatrices.clone() : null;
    }

    /**
     * Inverse rest transform of joint {@code index}. Read-only: the matrix is shared by every copy of the model.
     *
     * @throws IllegalStateException if the mesh is not bound
     */
    public Matrix4fc inverseBindMatrix(int index) {
        if (inverseBindMatrices == null) {
            throw new IllegalStateException("Skinned mesh '" + name + "' is not bound");
        }

        return inverseBindMatrices[index];
    }

    /**
     * Returns a bound copy of this mesh.
     *
     * @param restJointMatrices model-space rest transform of each joint, parallel to {@link #joints()}
     */
    public GeoSkinnedMesh withBindPose(Matrix4fc[] restJointMatrices) {
        if (restJointMatrices.length != joints.length) {
            throw new IllegalArgumentException(
                "Skinned mesh '" + name + "' has " + joints.length + " joints but " + restJointMatrices.length
                    + " rest matrices were given"
            );
        }

        Matrix4fc[] inverse = new Matrix4fc[restJointMatrices.length];

        for (int i = 0; i < restJointMatrices.length; i++) {
            inverse[i] = new Matrix4f(restJointMatrices[i]).invert();
        }

        return new GeoSkinnedMesh(name, joints, polygons, influenceJoints, influenceWeights, inverse);
    }

    public boolean isEmpty() {
        return polygons.quadCount() == 0;
    }
}
