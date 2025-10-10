package mod.azure.azurelib.animation.controller.keyframe;

import mod.azure.azurelib.core.math.IValue;

/**
 * A record of a deserialized animation for a given bone.<br>
 * Responsible for holding the various {@link AzKeyframe Keyframes} for the bone's animation transformations
 *
 * @param boneName          The name of the bone as listed in the {@code animation.json}
 * @param rotationKeyframes The deserialized rotation {@code Keyframe} stack
 * @param positionKeyframes The deserialized position {@code Keyframe} stack
 * @param scaleKeyframes    The deserialized scale {@code Keyframe} stack
 */
public record AzBoneAnimation(
    String boneName,
    AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes,
    AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes,
    AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes
) {}
