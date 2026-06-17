package mod.azure.azurelib.animation;

import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimationQueue;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingUtil;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

public class AzBoneAnimationUpdateUtil {

    public static void updatePositions(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot snapshot
    ) {
        var posXPoint = boneAnimation.pollPosX();
        var posYPoint = boneAnimation.pollPosY();
        var posZPoint = boneAnimation.pollPosZ();

        if (posXPoint != null && posYPoint != null && posZPoint != null) {
            bone.setPosX((float) AzEasingUtil.lerpWithOverride(posXPoint, easingType));
            bone.setPosY((float) AzEasingUtil.lerpWithOverride(posYPoint, easingType));
            bone.setPosZ((float) AzEasingUtil.lerpWithOverride(posZPoint, easingType));
            snapshot.updateOffset(bone.getPosX(), bone.getPosY(), bone.getPosZ());
            snapshot.startPosAnim();
            bone.markPositionAsChanged();
        }
    }

    public static void updateRotations(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot initialSnapshot,
        AzBoneSnapshot snapshot
    ) {
        var rotXPoint = boneAnimation.pollRotX();
        var rotYPoint = boneAnimation.pollRotY();
        var rotZPoint = boneAnimation.pollRotZ();

        if (rotXPoint != null && rotYPoint != null && rotZPoint != null) {
            bone.setRotX((float) AzEasingUtil.lerpWithOverride(rotXPoint, easingType) + initialSnapshot.getRotX());
            bone.setRotY((float) AzEasingUtil.lerpWithOverride(rotYPoint, easingType) + initialSnapshot.getRotY());
            bone.setRotZ((float) AzEasingUtil.lerpWithOverride(rotZPoint, easingType) + initialSnapshot.getRotZ());
            snapshot.updateRotation(bone.getRotX(), bone.getRotY(), bone.getRotZ());
            snapshot.startRotAnim();
            bone.markRotationAsChanged();
        }
    }

    public static void updateScale(
        AzBoneAnimationQueue boneAnimation,
        AzBone bone,
        AzEasingType easingType,
        AzBoneSnapshot snapshot
    ) {
        var scaleXPoint = boneAnimation.pollSclX();
        var scaleYPoint = boneAnimation.pollSclY();
        var scaleZPoint = boneAnimation.pollSclZ();

        if (scaleXPoint != null && scaleYPoint != null && scaleZPoint != null) {
            bone.setScaleX((float) AzEasingUtil.lerpWithOverride(scaleXPoint, easingType));
            bone.setScaleY((float) AzEasingUtil.lerpWithOverride(scaleYPoint, easingType));
            bone.setScaleZ((float) AzEasingUtil.lerpWithOverride(scaleZPoint, easingType));
            snapshot.updateScale(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
            snapshot.startScaleAnim();
            bone.markScaleAsChanged();
        }
    }
}
