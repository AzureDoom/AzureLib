/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.rewrite.model;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.*;

import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.cache.object.GeoCube;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.state.BoneSnapshot;

/**
 * Mutable bone object representing a set of cubes, as well as child bones.<br>
 * This is the object that is directly modified by animations to handle movement
 */
public class AzBone implements CoreGeoBone {

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
        this.hidden = metadata.dontRender() == Boolean.TRUE;

        this.position = new Vector3f();
        this.pivot = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f(1, 1, 1);

        this.worldSpaceNormal.identity();
        this.worldSpaceMatrix.identity();
        this.localSpaceMatrix.identity();
        this.modelSpaceMatrix.identity();
    }

    @Override
    public String getName() {
        return metadata.name();
    }

    @Override
    public AzBone getParent() {
        return metadata.parent();
    }

    @Override
    public float getRotX() {
        return this.rotation.x;
    }

    @Override
    public void setRotX(float value) {
        this.rotation.x = value;

        markRotationAsChanged();
    }

    @Override
    public float getRotY() {
        return this.rotation.y;
    }

    @Override
    public void setRotY(float value) {
        this.rotation.y = value;

        markRotationAsChanged();
    }

    @Override
    public float getRotZ() {
        return this.rotation.z;
    }

    @Override
    public void setRotZ(float value) {
        this.rotation.z = value;

        markRotationAsChanged();
    }

    @Override
    public float getPosX() {
        return this.position.x;
    }

    @Override
    public void setPosX(float value) {
        this.position.x = value;

        markPositionAsChanged();
    }

    @Override
    public float getPosY() {
        return this.position.y;
    }

    @Override
    public void setPosY(float value) {
        this.position.y = value;

        markPositionAsChanged();
    }

    @Override
    public float getPosZ() {
        return this.position.z;
    }

    @Override
    public void setPosZ(float value) {
        this.position.z = value;

        markPositionAsChanged();
    }

    @Override
    public float getScaleX() {
        return this.scale.x;
    }

    @Override
    public void setScaleX(float value) {
        this.scale.x = value;

        markScaleAsChanged();
    }

    @Override
    public float getScaleY() {
        return this.scale.y;
    }

    @Override
    public void setScaleY(float value) {
        this.scale.y = value;

        markScaleAsChanged();
    }

    @Override
    public float getScaleZ() {
        return this.scale.z;
    }

    @Override
    public void setScaleZ(float value) {
        this.scale.z = value;

        markScaleAsChanged();
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public void setHidden(boolean hidden) {
        this.hidden = hidden;

        setChildrenHidden(hidden);
    }

    @Override
    public void setChildrenHidden(boolean hideChildren) {
        this.childrenHidden = hideChildren;
    }

    @Override
    public float getPivotX() {
        return this.pivot.x;
    }

    @Override
    public void setPivotX(float value) {
        this.pivot.x = value;
    }

    @Override
    public float getPivotY() {
        return this.pivot.y;
    }

    @Override
    public void setPivotY(float value) {
        this.pivot.y = value;
    }

    @Override
    public float getPivotZ() {
        return this.pivot.z;
    }

    @Override
    public void setPivotZ(float value) {
        this.pivot.z = value;
    }

    @Override
    public boolean isHidingChildren() {
        return this.childrenHidden;
    }

    @Override
    public void markScaleAsChanged() {
        this.scaleChanged = true;
    }

    @Override
    public void markRotationAsChanged() {
        this.rotationChanged = true;
    }

    @Override
    public void markPositionAsChanged() {
        this.positionChanged = true;
    }

    @Override
    public boolean hasScaleChanged() {
        return this.scaleChanged;
    }

    @Override
    public boolean hasRotationChanged() {
        return this.rotationChanged;
    }

    @Override
    public boolean hasPositionChanged() {
        return this.positionChanged;
    }

    @Override
    public void resetStateChanges() {
        this.scaleChanged = false;
        this.rotationChanged = false;
        this.positionChanged = false;
    }

    /**
     * @deprecated DO NOT USE OR I WILL FIND YOU.
     */
    @Override
    @Deprecated(forRemoval = true)
    public BoneSnapshot getInitialSnapshot() {
        throw new UnsupportedOperationException();
    }

    public AzBoneSnapshot getInitialAzSnapshot() {
        return this.initialSnapshot;
    }

    @Override
    public List<AzBone> getChildBones() {
        return this.children;
    }

    @Override
    public void saveInitialSnapshot() {
        if (this.initialSnapshot == null) {
            this.initialSnapshot = new AzBoneSnapshot(this);
        }
    }

    public Boolean getMirror() {
        return metadata.mirror();
    }

    public Double getInflate() {
        return metadata.inflate();
    }

    public Boolean shouldNeverRender() {
        return metadata.dontRender();
    }

    public Boolean getReset() {
        return metadata.reset();
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
        this.modelSpaceMatrix.set(matrix);
    }

    public Matrix4f getLocalSpaceMatrix() {
        setTrackingMatrices(true);

        return this.localSpaceMatrix;
    }

    public void setLocalSpaceMatrix(Matrix4f matrix) {
        this.localSpaceMatrix.set(matrix);
    }

    public Matrix4f getWorldSpaceMatrix() {
        setTrackingMatrices(true);

        return this.worldSpaceMatrix;
    }

    public void setWorldSpaceMatrix(Matrix4f matrix) {
        this.worldSpaceMatrix.set(matrix);
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
        Vector4f vec = getLocalSpaceMatrix().transform(new Vector4f(0, 0, 0, 1));

        return new Vector3d(vec.x(), vec.y(), vec.z());
    }

    /**
     * Get the position of the bone relative to the model it belongs to
     */
    public Vector3d getModelPosition() {
        Vector4f vec = getModelSpaceMatrix().transform(new Vector4f(0, 0, 0, 1));

        return new Vector3d(-vec.x() * 16f, vec.y() * 16f, vec.z() * 16f);
    }

    public void setModelPosition(Vector3d pos) {
        // Doesn't work on bones with parent transforms
        AzBone parent = metadata.parent();
        Matrix4f matrix = (parent == null ? new Matrix4f().identity() : new Matrix4f(parent.getModelSpaceMatrix()))
            .invert();
        Vector4f vec = matrix.transform(
            new Vector4f(-(float) pos.x / 16f, (float) pos.y / 16f, (float) pos.z / 16f, 1)
        );

        updatePosition(-vec.x() * 16f, vec.y() * 16f, vec.z() * 16f);
    }

    /**
     * Get the position of the bone relative to the world
     */
    public Vector3d getWorldPosition() {
        Vector4f vec = getWorldSpaceMatrix().transform(new Vector4f(0, 0, 0, 1));

        return new Vector3d(vec.x(), vec.y(), vec.z());
    }

    public Matrix4f getModelRotationMatrix() {
        Matrix4f matrix = new Matrix4f(getModelSpaceMatrix());
        matrix.m03(0);
        matrix.m13(0);
        matrix.m23(0);

        return matrix;
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

    @Override
    public void updateRotation(float xRot, float yRot, float zRot) {
        setRotX(xRot);
        setRotY(yRot);
        setRotZ(zRot);
    }

    @Override
    public void updatePosition(float posX, float posY, float posZ) {
        setPosX(posX);
        setPosY(posY);
        setPosZ(posZ);
    }

    @Override
    public void updateScale(float scaleX, float scaleY, float scaleZ) {
        setScaleX(scaleX);
        setScaleY(scaleY);
        setScaleZ(scaleZ);
    }

    @Override
    public void updatePivot(float pivotX, float pivotY, float pivotZ) {
        setPivotX(pivotX);
        setPivotY(pivotY);
        setPivotZ(pivotZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getName(),
            (getParent() != null ? getParent().getName() : 0),
            getCubes().size(),
            getChildBones().size()
        );
    }
}
