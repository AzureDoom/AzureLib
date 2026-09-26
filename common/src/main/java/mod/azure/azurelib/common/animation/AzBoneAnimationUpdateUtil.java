package mod.azure.azurelib.common.animation;

import mod.azure.azurelib.common.animation.controller.AzBlendMode;
import mod.azure.azurelib.common.animation.controller.keyframe.AzBoneAnimationQueue;
import mod.azure.azurelib.common.animation.easing.AzEasingType;
import mod.azure.azurelib.common.animation.easing.AzEasingUtil;
import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.model.AzBoneSnapshot;

/**
 * Applies a controller's animation values for this frame to its bones.
 * <p>
 * Controllers are applied in registration order. Each one combines its pose with the bone's current value: whatever an
 * earlier controller wrote to that channel this frame, or the bind pose if none did.
 * <ul>
 * <li>{@link AzBlendMode#OVERRIDE}: blends toward the pose by the controller's weight. At weight 1 the pose is written
 * as-is, so the last controller to animate a channel wins, exactly as before blending existed.</li>
 * <li>{@link AzBlendMode#ADDITIVE}: adds the pose's offset from the bind pose (scale: multiplies by its ratio to the
 * bind scale), scaled by the weight.</li>
 * </ul>
 * At weight 0 the controller leaves the bone untouched.
 */
public class AzBoneAnimationUpdateUtil {

    public static void updatePositions(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot snapshot
    ) {
        updatePositions(
            boneAnimation,
            bone,
            easingType,
            bone.getInitialAzSnapshot(),
            snapshot,
            1,
            AzBlendMode.OVERRIDE,
            -1
        );
    }

    public static void updatePositions(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot initialSnapshot,
        AzBoneSnapshot snapshot,
        double weight,
        AzBlendMode blendMode,
        long frame
    ) {
        var posXPoint = boneAnimation.pollPosX();
        var posYPoint = boneAnimation.pollPosY();
        var posZPoint = boneAnimation.pollPosZ();

        if (posXPoint == null || posYPoint == null || posZPoint == null || weight <= 0) {
            return;
        }

        float x = (float) AzEasingUtil.lerpWithOverride(posXPoint, easingType);
        float y = (float) AzEasingUtil.lerpWithOverride(posYPoint, easingType);
        float z = (float) AzEasingUtil.lerpWithOverride(posZPoint, easingType);

        if (blendMode == AzBlendMode.ADDITIVE || weight < 1) {
            boolean layered = snapshot.isPositionWrittenInFrame(frame);
            float w = (float) weight;
            float baseX = layered ? bone.getPosX() : initialSnapshot.getOffsetX();
            float baseY = layered ? bone.getPosY() : initialSnapshot.getOffsetY();
            float baseZ = layered ? bone.getPosZ() : initialSnapshot.getOffsetZ();

            if (blendMode == AzBlendMode.ADDITIVE) {
                x = baseX + (x - initialSnapshot.getOffsetX()) * w;
                y = baseY + (y - initialSnapshot.getOffsetY()) * w;
                z = baseZ + (z - initialSnapshot.getOffsetZ()) * w;
            } else {
                x = blend(baseX, x, w);
                y = blend(baseY, y, w);
                z = blend(baseZ, z, w);
            }
        }

        bone.setPosX(x);
        bone.setPosY(y);
        bone.setPosZ(z);
        snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
        snapshot.startPosAnim();
        snapshot.markPositionWritten(frame);
        bone.markPositionAsChanged();
    }

    public static void updateRotations(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot initialSnapshot,
        AzBoneSnapshot snapshot
    ) {
        updateRotations(boneAnimation, bone, easingType, initialSnapshot, snapshot, 1, AzBlendMode.OVERRIDE, -1);
    }

    public static void updateRotations(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot initialSnapshot,
        AzBoneSnapshot snapshot,
        double weight,
        AzBlendMode blendMode,
        long frame
    ) {
        var rotXPoint = boneAnimation.pollRotX();
        var rotYPoint = boneAnimation.pollRotY();
        var rotZPoint = boneAnimation.pollRotZ();

        if (rotXPoint == null || rotYPoint == null || rotZPoint == null || weight <= 0) {
            return;
        }

        // Keyframe rotations are offsets from the bind rotation.
        float x = (float) AzEasingUtil.lerpWithOverride(rotXPoint, easingType) + initialSnapshot.getRotX();
        float y = (float) AzEasingUtil.lerpWithOverride(rotYPoint, easingType) + initialSnapshot.getRotY();
        float z = (float) AzEasingUtil.lerpWithOverride(rotZPoint, easingType) + initialSnapshot.getRotZ();

        if (blendMode == AzBlendMode.ADDITIVE || weight < 1) {
            // Per-axis Euler blending, consistent with how keyframes themselves interpolate.
            boolean layered = snapshot.isRotationWrittenInFrame(frame);
            float w = (float) weight;
            float baseX = layered ? bone.getRotX() : initialSnapshot.getRotX();
            float baseY = layered ? bone.getRotY() : initialSnapshot.getRotY();
            float baseZ = layered ? bone.getRotZ() : initialSnapshot.getRotZ();

            if (blendMode == AzBlendMode.ADDITIVE) {
                x = baseX + (x - initialSnapshot.getRotX()) * w;
                y = baseY + (y - initialSnapshot.getRotY()) * w;
                z = baseZ + (z - initialSnapshot.getRotZ()) * w;
            } else {
                x = blend(baseX, x, w);
                y = blend(baseY, y, w);
                z = blend(baseZ, z, w);
            }
        }

        bone.setRotX(x);
        bone.setRotY(y);
        bone.setRotZ(z);
        snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
        snapshot.startRotAnim();
        snapshot.markRotationWritten(frame);
        bone.markRotationAsChanged();
    }

    public static void updateScale(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot snapshot
    ) {
        updateScale(
            boneAnimation,
            bone,
            easingType,
            bone.getInitialAzSnapshot(),
            snapshot,
            1,
            AzBlendMode.OVERRIDE,
            -1
        );
    }

    public static void updateScale(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot initialSnapshot,
        AzBoneSnapshot snapshot,
        double weight,
        AzBlendMode blendMode,
        long frame
    ) {
        var scaleXPoint = boneAnimation.pollSclX();
        var scaleYPoint = boneAnimation.pollSclY();
        var scaleZPoint = boneAnimation.pollSclZ();

        if (scaleXPoint == null || scaleYPoint == null || scaleZPoint == null || weight <= 0) {
            return;
        }

        float x = (float) AzEasingUtil.lerpWithOverride(scaleXPoint, easingType);
        float y = (float) AzEasingUtil.lerpWithOverride(scaleYPoint, easingType);
        float z = (float) AzEasingUtil.lerpWithOverride(scaleZPoint, easingType);

        if (blendMode == AzBlendMode.ADDITIVE || weight < 1) {
            boolean layered = snapshot.isScaleWrittenInFrame(frame);
            float w = (float) weight;
            float baseX = layered ? bone.getScaleX() : initialSnapshot.getScaleX();
            float baseY = layered ? bone.getScaleY() : initialSnapshot.getScaleY();
            float baseZ = layered ? bone.getScaleZ() : initialSnapshot.getScaleZ();

            if (blendMode == AzBlendMode.ADDITIVE) {
                // Scale multiplies: a keyframe of 1.2 over a bind scale of 1 grows whatever is underneath by 20%.
                x = baseX * blend(1, ratio(x, initialSnapshot.getScaleX()), w);
                y = baseY * blend(1, ratio(y, initialSnapshot.getScaleY()), w);
                z = baseZ * blend(1, ratio(z, initialSnapshot.getScaleZ()), w);
            } else {
                x = blend(baseX, x, w);
                y = blend(baseY, y, w);
                z = blend(baseZ, z, w);
            }
        }

        bone.setScaleX(x);
        bone.setScaleY(y);
        bone.setScaleZ(z);
        snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
        snapshot.startScaleAnim();
        snapshot.markScaleWritten(frame);
        bone.markScaleAsChanged();
    }

    private static float blend(float from, float to, float weight) {
        return from + (to - from) * weight;
    }

    /** A scale keyframe relative to the bind scale; a bind scale of 0 has no meaningful ratio, so use the value. */
    private static float ratio(float value, float bindScale) {
        return bindScale == 0 ? value : value / bindScale;
    }
}
