package mod.azure.azurelib.animation.primitive;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimation;

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
    AzKeyframes keyframes
) {

}
