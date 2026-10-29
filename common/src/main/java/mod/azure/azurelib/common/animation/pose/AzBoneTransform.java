package mod.azure.azurelib.common.animation.pose;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.model.AzBoneSnapshot;

/**
 * A bone's local transform as one mutable value: position (model pixels), rotation (radians, applied Z then Y then X)
 * and scale. It is the unit the pose layer reads and writes, and can be used on its own to save, restore, blend or copy
 * bone poses without juggling nine floats.
 * <p>
 * Instances are meant to be reused; none of the methods allocate.
 * </p>
 */
public final class AzBoneTransform {

    private final Vector3f position = new Vector3f();

    private final Vector3f rotation = new Vector3f();

    private final Vector3f scale = new Vector3f(1, 1, 1);

    public Vector3f position() {
        return position;
    }

    public Vector3f rotation() {
        return rotation;
    }

    public Vector3f scale() {
        return scale;
    }

    public AzBoneTransform setIdentity() {
        position.zero();
        rotation.zero();
        scale.set(1, 1, 1);
        return this;
    }

    public AzBoneTransform set(AzBoneTransform other) {
        position.set(other.position);
        rotation.set(other.rotation);
        scale.set(other.scale);
        return this;
    }

    public AzBoneTransform set(Vector3fc position, Vector3fc rotation, Vector3fc scale) {
        this.position.set(position);
        this.rotation.set(rotation);
        this.scale.set(scale);
        return this;
    }

    public AzBoneTransform setFromBone(AzBone bone) {
        position.set(bone.getPosX(), bone.getPosY(), bone.getPosZ());
        rotation.set(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        scale.set(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        return this;
    }

    public AzBoneTransform setFromSnapshot(AzBoneSnapshot snapshot) {
        position.set(snapshot.getOffsetX(), snapshot.getOffsetY(), snapshot.getOffsetZ());
        rotation.set(snapshot.getRotX(), snapshot.getRotY(), snapshot.getRotZ());
        scale.set(snapshot.getScaleX(), snapshot.getScaleY(), snapshot.getScaleZ());
        return this;
    }

    public AzBoneTransform setFromBind(AzBone bone) {
        AzBoneSnapshot bind = bone.getInitialAzSnapshot();
        return bind == null ? setIdentity() : setFromSnapshot(bind);
    }

    public void applyTo(AzBone bone) {
        bone.updatePosition(position.x, position.y, position.z);
        bone.updateRotation(rotation.x, rotation.y, rotation.z);
        bone.updateScale(scale.x, scale.y, scale.z);
    }

    public void applyToUnmarked(AzBone bone) {
        bone.setTransformUnmarked(position, rotation, scale);
    }

    public boolean matches(AzBone bone) {
        return position.x == bone.getPosX() && position.y == bone.getPosY() && position.z == bone.getPosZ()
            && rotation.x == bone.getRotX() && rotation.y == bone.getRotY() && rotation.z == bone.getRotZ()
            && scale.x == bone.getScaleX() && scale.y == bone.getScaleY() && scale.z == bone.getScaleZ();
    }

    public AzBoneTransform blendTowards(AzBoneTransform target, float weight) {
        blend(position, target.position, weight);
        blend(rotation, target.rotation, weight);
        blend(scale, target.scale, weight);
        return this;
    }

    public AzBoneTransform addOffset(AzBoneTransform pose, AzBoneTransform bind, float weight) {
        position.set(
            AzTransformMath.additive(position.x, pose.position.x, bind.position.x, weight),
            AzTransformMath.additive(position.y, pose.position.y, bind.position.y, weight),
            AzTransformMath.additive(position.z, pose.position.z, bind.position.z, weight)
        );
        rotation.set(
            AzTransformMath.additive(rotation.x, pose.rotation.x, bind.rotation.x, weight),
            AzTransformMath.additive(rotation.y, pose.rotation.y, bind.rotation.y, weight),
            AzTransformMath.additive(rotation.z, pose.rotation.z, bind.rotation.z, weight)
        );
        scale.set(
            AzTransformMath.additiveScale(scale.x, pose.scale.x, bind.scale.x, weight),
            AzTransformMath.additiveScale(scale.y, pose.scale.y, bind.scale.y, weight),
            AzTransformMath.additiveScale(scale.z, pose.scale.z, bind.scale.z, weight)
        );
        return this;
    }

    private static void blend(Vector3f from, Vector3fc to, float weight) {
        from.set(
            AzTransformMath.blend(from.x, to.x(), weight),
            AzTransformMath.blend(from.y, to.y(), weight),
            AzTransformMath.blend(from.z, to.z(), weight)
        );
    }

    @Override
    public String toString() {
        return "AzBoneTransform[pos=" + position + ", rot=" + rotation + ", scale=" + scale + "]";
    }
}
