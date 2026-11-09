package mod.azure.azurelib.common.animation.primitive;

import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.keyframe.AzBoneAnimation;

/**
 * A compiled animation instance for use by the {@link AzAnimationController}<br>
 * Modifications or extensions of a compiled Animation are not supported, and therefore an instance of
 * <code>Animation</code> is considered final and immutable.
 */
public record AzBakedAnimation(
    String name,
    double length,
    AzLoopType loopType,
    AzBoneAnimation[] boneAnimations,
    AzKeyframes keyframes,
    AzAnimationDefaults defaults,
    AzPoseWeights poseWeights
) {

    public AzBakedAnimation {
        poseWeights = poseWeights == null ? AzPoseWeights.NONE : poseWeights;
    }

    public AzBakedAnimation(
        String name,
        double length,
        AzLoopType loopType,
        AzBoneAnimation[] boneAnimations,
        AzKeyframes keyframes,
        AzAnimationDefaults defaults
    ) {
        this(name, length, loopType, boneAnimations, keyframes, defaults, AzPoseWeights.NONE);
    }

    public AzBakedAnimation(
        String name,
        double length,
        AzLoopType loopType,
        AzBoneAnimation[] boneAnimations,
        AzKeyframes keyframes
    ) {
        this(name, length, loopType, boneAnimations, keyframes, AzAnimationDefaults.NONE, AzPoseWeights.NONE);
    }
}
