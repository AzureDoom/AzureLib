package mod.azure.azurelib.animation.pose;

import mod.azure.azurelib.model.AzBone;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * A pose modifier's handle on one bone for the current frame.
 * <p>
 * Reads come in three flavors:
 * </p>
 * <ul>
 * <li><b>bind</b>: the model's rest pose.</li>
 * <li><b>animated</b>: the pose the animation controllers produced this frame, before any modifier ran.</li>
 * <li><b>current</b>: the animated pose plus everything earlier modifiers (and earlier writes by this modifier) have
 * done to it.</li>
 * </ul>
 * <p>
 * Writes change the current pose and are scaled by the running modifier's weight. They are applied to the real bone
 * once all modifiers have run, and undone before the next frame's controllers run. Additive writes ({@link #rotate},
 * {@link #translate}, {@link #scale}) layer on top of the current pose; absolute writes ({@link #setRotation},
 * {@link #setPosition}, {@link #setScale}) blend the current pose toward the given value. Writes to a bone outside the
 * modifier's mask, or to a bone the model does not have, are ignored.
 * </p>
 * <p>
 * Units match {@link AzBone}: positions in model pixels (1/16 block), rotations in radians.
 * </p>
 */
public final class AzPoseBone {

    static final AzPoseBone MISSING = new AzPoseBone(null, null);

    private final @Nullable AzPoseLayer<?> layer;

    private final @Nullable AzBone bone;

    final AzBoneTransform bind = new AzBoneTransform();

    final AzBoneTransform animated = new AzBoneTransform();

    final AzBoneTransform current = new AzBoneTransform();

    final AzBoneTransform committed = new AzBoneTransform();

    private long syncFrame = Long.MIN_VALUE;

    private long touchFrame = Long.MIN_VALUE;

    AzPoseBone(@Nullable AzPoseLayer<?> layer, @Nullable AzBone bone) {
        this.layer = layer;
        this.bone = bone;

        if (bone != null) {
            bind.setFromBind(bone);
        }
    }

    public boolean exists() {
        return bone != null;
    }

    public @Nullable AzBone bone() {
        return bone;
    }

    public boolean isWritable() {
        return bone != null && layer != null && layer.canWrite(bone);
    }

    public Vector3fc bindPosition() {
        return bind.position();
    }

    public Vector3fc bindRotation() {
        return bind.rotation();
    }

    public Vector3fc bindScale() {
        return bind.scale();
    }

    public Vector3fc animatedPosition() {
        sync();
        return animated.position();
    }

    public Vector3fc animatedRotation() {
        sync();
        return animated.rotation();
    }

    public Vector3fc animatedScale() {
        sync();
        return animated.scale();
    }

    public Vector3fc currentPosition() {
        sync();
        return current.position();
    }

    public Vector3fc currentRotation() {
        sync();
        return current.rotation();
    }

    public Vector3fc currentScale() {
        sync();
        return current.scale();
    }

    public AzPoseBone rotate(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            current.rotation().add(x * w, y * w, z * w);
        }

        return this;
    }

    public AzPoseBone rotateDegrees(float x, float y, float z) {
        return rotate(x * Mth.DEG_TO_RAD, y * Mth.DEG_TO_RAD, z * Mth.DEG_TO_RAD);
    }

    public AzPoseBone translate(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            current.position().add(x * w, y * w, z * w);
        }

        return this;
    }

    public AzPoseBone scale(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            current.scale()
                .mul(AzTransformMath.blend(1, x, w), AzTransformMath.blend(1, y, w), AzTransformMath.blend(1, z, w));
        }

        return this;
    }

    public AzPoseBone scale(float factor) {
        return scale(factor, factor, factor);
    }

    public AzPoseBone setRotation(float x, float y, float z) {
        if (beginWrite()) {
            blendTowards(current.rotation(), x, y, z, layer.weight());
        }

        return this;
    }

    public AzPoseBone setPosition(float x, float y, float z) {
        if (beginWrite()) {
            blendTowards(current.position(), x, y, z, layer.weight());
        }

        return this;
    }

    public AzPoseBone setScale(float x, float y, float z) {
        if (beginWrite()) {
            blendTowards(current.scale(), x, y, z, layer.weight());
        }

        return this;
    }

    public AzPoseBone set(AzBoneTransform target) {
        if (beginWrite()) {
            current.blendTowards(target, layer.weight());
        }

        return this;
    }

    public AzPoseBone add(AzBoneTransform pose) {
        if (beginWrite()) {
            current.addOffset(pose, bind, layer.weight());
        }

        return this;
    }

    public AzPoseBone resetToBind() {
        return set(bind);
    }

    public AzPoseBone resetToAnimated() {
        if (beginWrite()) {
            current.blendTowards(animated, layer.weight());
        }

        return this;
    }

    private void sync() {
        if (bone == null || layer == null) {
            return;
        }

        long frame = layer.frame();

        if (syncFrame != frame) {
            syncFrame = frame;
            animated.setFromBone(bone);
            current.set(animated);
        }
    }

    private boolean beginWrite() {
        if (bone == null || layer == null || !layer.canWrite(bone)) {
            return false;
        }

        sync();

        long frame = layer.frame();

        if (touchFrame != frame) {
            touchFrame = frame;
            layer.markTouched(this);
        }

        return true;
    }

    void commit() {
        committed.set(current);
        current.applyToUnmarked(bone);
    }

    void restore() {
        if (bone != null && committed.matches(bone)) {
            animated.applyToUnmarked(bone);
        }
    }

    private static void blendTowards(Vector3f v, float x, float y, float z, float w) {
        v.set(AzTransformMath.blend(v.x, x, w), AzTransformMath.blend(v.y, y, w), AzTransformMath.blend(v.z, z, w));
    }
}
