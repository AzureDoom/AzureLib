package mod.azure.azurelib.animation.pose;

import com.mojang.math.Vector3f;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.model.AzBone;

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
 * 1.18.2 has no read-only vector type, so the read methods return the live backing vectors. Treat them as read-only:
 * change the pose through the write methods, never by mutating a returned vector.
 * </p>
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

    private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);

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

    public Vector3f bindPosition() {
        return bind.position();
    }

    public Vector3f bindRotation() {
        return bind.rotation();
    }

    public Vector3f bindScale() {
        return bind.scale();
    }

    public Vector3f animatedPosition() {
        sync();
        return animated.position();
    }

    public Vector3f animatedRotation() {
        sync();
        return animated.rotation();
    }

    public Vector3f animatedScale() {
        sync();
        return animated.scale();
    }

    public Vector3f currentPosition() {
        sync();
        return current.position();
    }

    public Vector3f currentRotation() {
        sync();
        return current.rotation();
    }

    public Vector3f currentScale() {
        sync();
        return current.scale();
    }

    public AzPoseBone rotate(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            add(current.rotation(), x * w, y * w, z * w);
        }

        return this;
    }

    public AzPoseBone rotateDegrees(float x, float y, float z) {
        return rotate(x * DEG_TO_RAD, y * DEG_TO_RAD, z * DEG_TO_RAD);
    }

    public AzPoseBone translate(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            add(current.position(), x * w, y * w, z * w);
        }

        return this;
    }

    public AzPoseBone scale(float x, float y, float z) {
        if (beginWrite()) {
            float w = layer.weight();
            Vector3f s = current.scale();
            s.set(
                s.x() * AzTransformMath.blend(1, x, w),
                s.y() * AzTransformMath.blend(1, y, w),
                s.z() * AzTransformMath.blend(1, z, w)
            );
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

    private static void add(Vector3f v, float x, float y, float z) {
        v.set(v.x() + x, v.y() + y, v.z() + z);
    }

    private static void blendTowards(Vector3f v, float x, float y, float z, float w) {
        v.set(
            AzTransformMath.blend(v.x(), x, w),
            AzTransformMath.blend(v.y(), y, w),
            AzTransformMath.blend(v.z(), z, w)
        );
    }
}
