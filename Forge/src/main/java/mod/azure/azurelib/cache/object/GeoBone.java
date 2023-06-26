package mod.azure.azurelib.cache.object;

import java.util.List;
import java.util.Objects;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.core.animatable.model.CoreGeoBone;
import mod.azure.azurelib.core.state.BoneSnapshot;

/**
 * Mutable bone object representing a set of cubes, as well as child bones.<br>
 * This is the object that is directly modified by animations to handle movement
 */
public class GeoBone implements CoreGeoBone {
	private final GeoBone parent;
	private final String name;

	private final List<GeoBone> children = new ObjectArrayList<>();
	private final List<GeoCube> cubes = new ObjectArrayList<>();

	private final Boolean mirror;
	private final Double inflate;
	private final Boolean dontRender;
	private final Boolean reset;

	private BoneSnapshot initialSnapshot;

	private boolean hidden;
	private boolean childrenHidden = false;

	private float scaleX = 1;
	private float scaleY = 1;
	private float scaleZ = 1;

	private float positionX;
	private float positionY;
	private float positionZ;

	private float pivotX;
	private float pivotY;
	private float pivotZ;

	private float rotX;
	private float rotY;
	private float rotZ;

	private boolean positionChanged = false;
	private boolean rotationChanged = false;
	private boolean scaleChanged = false;
	private final Matrix4f modelSpaceMatrix = new Matrix4f();
	private final Matrix4f localSpaceMatrix = new Matrix4f();
	private final Matrix4f worldSpaceMatrix = new Matrix4f();
	private Matrix3f worldSpaceNormal = new Matrix3f();

	private boolean trackingMatrices;

	public GeoBone(GeoBone parent, String name, Boolean mirror, Double inflate, Boolean dontRender, Boolean reset) {
		this.parent = parent;
		this.name = name;
		this.mirror = mirror;
		this.inflate = inflate;
		this.dontRender = dontRender;
		this.reset = reset;
		this.trackingMatrices = false;
		this.hidden = this.dontRender == Boolean.TRUE;

		this.worldSpaceNormal.setIdentity();
		this.worldSpaceMatrix.setIdentity();
		this.localSpaceMatrix.setIdentity();
		this.modelSpaceMatrix.setIdentity();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public GeoBone getParent() {
		return this.parent;
	}

	@Override
	public float getRotX() {
		return this.rotX;
	}

	@Override
	public float getRotY() {
		return this.rotY;
	}

	@Override
	public float getRotZ() {
		return this.rotZ;
	}

	@Override
	public float getPosX() {
		return this.positionX;
	}

	@Override
	public float getPosY() {
		return this.positionY;
	}

	@Override
	public float getPosZ() {
		return this.positionZ;
	}

	@Override
	public float getScaleX() {
		return this.scaleX;
	}

	@Override
	public float getScaleY() {
		return this.scaleY;
	}

	@Override
	public float getScaleZ() {
		return this.scaleZ;
	}

	@Override
	public void setRotX(float value) {
		this.rotX = value;

		markRotationAsChanged();
	}

	@Override
	public void setRotY(float value) {
		this.rotY = value;

		markRotationAsChanged();
	}

	@Override
	public void setRotZ(float value) {
		this.rotZ = value;

		markRotationAsChanged();
	}

	@Override
	public void setPosX(float value) {
		this.positionX = value;

		markPositionAsChanged();
	}

	@Override
	public void setPosY(float value) {
		this.positionY = value;

		markPositionAsChanged();
	}

	@Override
	public void setPosZ(float value) {
		this.positionZ = value;

		markPositionAsChanged();
	}

	@Override
	public void setScaleX(float value) {
		this.scaleX = value;

		markScaleAsChanged();
	}

	@Override
	public void setScaleY(float value) {
		this.scaleY = value;

		markScaleAsChanged();
	}

	@Override
	public void setScaleZ(float value) {
		this.scaleZ = value;

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
	public void setPivotX(float value) {
		this.pivotX = value;
	}

	@Override
	public void setPivotY(float value) {
		this.pivotY = value;
	}

	@Override
	public void setPivotZ(float value) {
		this.pivotZ = value;
	}

	@Override
	public float getPivotX() {
		return this.pivotX;
	}

	@Override
	public float getPivotY() {
		return this.pivotY;
	}

	@Override
	public float getPivotZ() {
		return this.pivotZ;
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

	@Override
	public BoneSnapshot getInitialSnapshot() {
		return this.initialSnapshot;
	}

	@Override
	public List<GeoBone> getChildBones() {
		return this.children;
	}

	@Override
	public void saveInitialSnapshot() {
		if (this.initialSnapshot == null)
			this.initialSnapshot = saveSnapshot();
	}

	public Boolean getMirror() {
		return this.mirror;
	}

	public Double getInflate() {
		return this.inflate;
	}

	public Boolean shouldNeverRender() {
		return this.dontRender;
	}

	public Boolean getReset() {
		return this.reset;
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
		this.localSpaceMatrix.load(matrix);
	}

	public Matrix4f getWorldSpaceMatrix() {
		setTrackingMatrices(true);

		return this.worldSpaceMatrix;
	}

	public void setWorldSpaceMatrix(Matrix4f matrix) {
		this.worldSpaceMatrix.multiply(matrix);
	}

	public void setWorldSpaceNormal(Matrix3f matrix) {
		this.worldSpaceNormal = matrix;
	}

	public Matrix3f getWorldSpaceNormal() {
		return worldSpaceNormal;
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

	/**
	 * Get the position of the bone relative to the world
	 */
	public Vector3d getWorldPosition() {
		Matrix4f matrix = getWorldSpaceMatrix();
		Vector4f vec = new Vector4f(0, 0, 0, 1);
		vec.transform(matrix);

		return new Vector3d(vec.x(), vec.y(), vec.z());
	}

	public void setModelPosition(Vector3d pos) {
		// Doesn't work on bones with parent transforms
		GeoBone parent = getParent();
		Matrix4f identity = new Matrix4f();
		identity.setIdentity();
		Matrix4f matrix = parent == null ? identity : parent.getModelSpaceMatrix().copy();
		matrix.invert();
		Vector4f vec = new Vector4f(-(float) pos.x / 16f, (float) pos.y / 16f, (float) pos.z / 16f, 1);
		vec.transform(matrix);

		updatePosition(-vec.x() * 16f, vec.y() * 16f, vec.z() * 16f);
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

	public void addRotationOffsetFromBone(GeoBone source) {
		setRotX(getRotX() + source.getRotX() - source.getInitialSnapshot().getRotX());
		setRotY(getRotY() + source.getRotY() - source.getInitialSnapshot().getRotY());
		setRotZ(getRotZ() + source.getRotZ() - source.getInitialSnapshot().getRotZ());
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
		return Objects.hash(getName(), (getParent() != null ? getParent().getName() : 0), getCubes().size(), getChildBones().size());
	}
}
