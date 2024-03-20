/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import mod.azure.azurelib.core.math.IValue;

/**
 * A record of a deserialized animation for a given bone.<br>
 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
 *
 * @param boneName          The name of the bone as listed in the {@code animation.json}
 * @param rotationKeyFrames The deserialized rotation {@code Keyframe} stack
 * @param positionKeyFrames The deserialized position {@code Keyframe} stack
 * @param scaleKeyFrames    The deserialized scale {@code Keyframe} stack
 */
public record BoneAnimation(
    String boneName,
    KeyframeStack<Keyframe<IValue>> rotationKeyFrames,
    KeyframeStack<Keyframe<IValue>> positionKeyFrames,
    KeyframeStack<Keyframe<IValue>> scaleKeyFrames
) {}
