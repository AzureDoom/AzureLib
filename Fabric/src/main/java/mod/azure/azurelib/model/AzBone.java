/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.model;

import com.mojang.math.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.util.RenderUtils;

/**
 * Mutable bone object representing a set of cubes, as well as child bones.<br>
 * This is the object that is directly modified by animations to handle movement
 */
public class AzBone {

    private final AzBoneMetadata metadata;

    private final List<AzBone> children = new ObjectArrayList<>();

    private final List<GeoCube> cubes = new ObjectArrayList<>();

    private final Matrix4f modelSpaceMatrix = new Matrix4f();

    private final Matrix4f localSpaceMatrix = new Matrix4f();

    private final Matrix4f worldSpaceMatrix = new Matrix4f();

    private AzBoneSnapshot initialSnapshot;

    private boolean hidden;

    private boolean childrenHidden = false;

    private final Vector3f pivot;

    private final Vector3f position;

    private final Vector3f rotation;

    private final Vector3f scale;

    private boolean positionChanged = false;

    private boolean rotationChanged = false;

    private boolean scaleChanged = false;

    private Matrix3f worldSpaceNormal = new Matrix3f();

    private boolean trackingMatrices;

    public AzBone(AzBoneMetadata metadata) {
        this.metadata = metadata;
        this.trackingMatrices = false;
        this.hidden = metadata.getDontRender() == Boolean.TRUE;

        this.position = new Vector3f();
        this.pivot = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f(1, 1, 1);

        this.worldSpaceNormal.setIdentity();
        this.worldSpaceMatrix.setIdentity();
        this.localSpaceMatrix.setIdentity();
        this.modelSpaceMatrix.setIdentity();
    }

    public String getName() {
        return metadata.getName();
    }

    public AzBone getParent() {
        return metadata.getParent();
    }

    public float getRotX() {
        return this.rotation.x();
    }

    public void setRotX(float value) {
        this.rotation.x = value;

        markRotationAsChanged();
    }

    public float getRotY() {
        return this.rotation.y();
    }

    public void setRotY(float value) {
        this.rotation.y = value;

        markRotationAsChanged();
    }

    public float getRotZ() {
        return this.rotation.z();
    }

    public void setRotZ(float value) {
        this.rotation.z = value;

        markRotationAsChanged();
    }

    public void updateRotation(float xRot, float yRot, float zRot) {
        setRotX(xRot);
        setRotY(yRot);
        setRotZ(zRot);
    }

    public float getPosX() {
        return this.position.x();
    }

    public void setPosX(float value) {
        this.position.x = value;

        markPositionAsChanged();
    }

    public float getPosY() {
        return this.position.y();
    }

    public void setPosY(float value) {
        this.position.y = value;

        markPositionAsChanged();
    }

    public float getPosZ() {
        return this.position.z();
    }

    public void setPosZ(float value) {
        this.position.z = value;

        markPositionAsChanged();
    }

    public void updatePosition(float posX, float posY, float posZ) {
        setPosX(posX);
        setPosY(posY);
        setPosZ(posZ);
    }

    public float getScaleX() {
        return this.scale.x();
    }

    public void setScaleX(float value) {
        this.scale.x = value;

        markScaleAsChanged();
    }

    public void updateScale(float scaleX, float scaleY, float scaleZ) {
        setScaleX(scaleX);
        setScaleY(scaleY);
        setScaleZ(scaleZ);
    }

    public float getScaleY() {
        return this.scale.y();
    }

    public void setScaleY(float value) {
        this.scale.y = value;

        markScaleAsChanged();
    }

    public float getScaleZ() {
        return this.scale.z();
    }

    public void setScaleZ(float value) {
        this.scale.z = value;

        markScaleAsChanged();
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;

        setChildrenHidden(hidden);
    }

    public void setChildrenHidden(boolean hideChildren) {
        this.childrenHidden = hideChildren;
    }

    public float getPivotX() {
        return this.pivot.x();
    }

    public void setPivotX(float value) {
        this.pivot.x = value;
    }

    public float getPivotY() {
        return this.pivot.y();
    }

    public void setPivotY(float value) {
        this.pivot.y = value;
    }

    public float getPivotZ() {
        return this.pivot.z();
    }

    public void setPivotZ(float value) {
        this.pivot.z = value;
    }

    public void updatePivot(float pivotX, float pivotY, float pivotZ) {
        setPivotX(pivotX);
        setPivotY(pivotY);
        setPivotZ(pivotZ);
    }

    public boolean isHidingChildren() {
        return this.childrenHidden;
    }

    public void markScaleAsChanged() {
        this.scaleChanged = true;
    }

    public void markRotationAsChanged() {
        this.rotationChanged = true;
    }

    public void markPositionAsChanged() {
        this.positionChanged = true;
    }

    public boolean hasScaleChanged() {
        return this.scaleChanged;
    }

    public boolean hasRotationChanged() {
        return this.rotationChanged;
    }

    public boolean hasPositionChanged() {
        return this.positionChanged;
    }

    public void resetStateChanges() {
        this.scaleChanged = false;
        this.rotationChanged = false;
        this.positionChanged = false;
    }

    public AzBoneSnapshot getInitialAzSnapshot() {
        return this.initialSnapshot;
    }

    public List<AzBone> getChildBones() {
        return this.children;
    }

    public void saveInitialSnapshot() {
        if (this.initialSnapshot == null) {
            this.initialSnapshot = new AzBoneSnapshot(this);
        }
    }

    public Boolean getMirror() {
        return metadata.getMirror();
    }

    public Double getInflate() {
        return metadata.getInflate();
    }

    public Boolean shouldNeverRender() {
        return metadata.getDontRender();
    }

    public Boolean getReset() {
        return metadata.getReset();
    }

    public List<GeoCube> getCubes() {
        return this.cubes;
    }

    public boolean isTrackingMatrices() {
        return trackingMatrices;
    }

    public void setTrackingMatrices(boolean trackingMatrices) {
        this.trackingMatrices = trackingMatrices;
    }

    public Matrix4f getModelSpaceMatrix() {
        setTrackingMatrices(true);

        return this.modelSpaceMatrix;
    }

    public void setModelSpaceMatrix(Matrix4f matrix) {
        this.modelSpaceMatrix.multiply(matrix);
    }

    public Matrix4f getLocalSpaceMatrix() {
        setTrackingMatrices(true);

        return this.localSpaceMatrix;
    }

    public void setLocalSpaceMatrix(Matrix4f matrix) {
        RenderUtils.copy(this.localSpaceMatrix, matrix);
    }

    public Matrix4f getWorldSpaceMatrix() {
        setTrackingMatrices(true);

        return this.worldSpaceMatrix;
    }

    public void setWorldSpaceMatrix(Matrix4f matrix) {
        this.worldSpaceMatrix.multiply(matrix);
    }

    public Matrix3f getWorldSpaceNormal() {
        return worldSpaceNormal;
    }

    public void setWorldSpaceNormal(Matrix3f matrix) {
        this.worldSpaceNormal = matrix;
    }

    /**
     * Get the position of the bone relative to its owner
     */
    public Vector3d getLocalPosition() {
        Matrix4f matrix = getLocalSpaceMatrix();
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(matrix);
        return new Vector3d(vec.x(), vec.y(), vec.z());
    }

    /**
     * Get the position of the bone relative to the model it belongs to
     */
    public Vector3d getModelPosition() {
        Matrix4f matrix = getModelSpaceMatrix();
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(matrix);
        return new Vector3d(-vec.x() * 16f, vec.y() * 16f, vec.z() * 16f);
    }

    public void setModelPosition(Vector3d pos) {
        // Doesn't work on bones with parent transforms
        AzBone parent = getParent();
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        Matrix4f matrix = parent == null ? identity : parent.getModelSpaceMatrix().copy();
        matrix.invert();
        Vector4f vec = new Vector4f(-(float) pos.x / 16f, (float) pos.y / 16f, (float) pos.z / 16f, 1);
        vec.transform(matrix);

        updatePosition(-vec.x() * 16f, vec.y() * 16f, vec.z() * 16f);
    }

    /**
     * Get the position of the bone relative to the world
     */
    public Vector3d getWorldPosition() {
        Matrix4f matrix = getWorldSpaceMatrix();
        Vector4f vec = new Vector4f(0, 0, 0, 1);
        vec.transform(matrix);

        return new Vector3d(vec.x(), vec.y(), vec.z());
    }

    public Matrix4f getModelRotationMatrix() {
        Matrix4f matrix = getModelSpaceMatrix().copy();
        removeMatrixTranslation(matrix);

        return matrix;
    }

    public static void removeMatrixTranslation(Matrix4f matrix) {
        matrix.m03 = 0;
        matrix.m13 = 0;
        matrix.m23 = 0;
    }

    public Vector3d getPositionVector() {
        return new Vector3d(getPosX(), getPosY(), getPosZ());
    }

    public Vector3d getRotationVector() {
        return new Vector3d(getRotX(), getRotY(), getRotZ());
    }

    public Vector3d getScaleVector() {
        return new Vector3d(getScaleX(), getScaleY(), getScaleZ());
    }

    public void addRotationOffsetFromBone(AzBone source) {
        setRotX(getRotX() + source.getRotX() - source.getInitialAzSnapshot().getRotX());
        setRotY(getRotY() + source.getRotY() - source.getInitialAzSnapshot().getRotY());
        setRotZ(getRotZ() + source.getRotZ() - source.getInitialAzSnapshot().getRotZ());
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return hashCode() == obj.hashCode();
    }
}
