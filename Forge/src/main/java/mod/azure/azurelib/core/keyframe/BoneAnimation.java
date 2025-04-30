/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import mod.azure.azurelib.core.math.IValue;


@Deprecated()
public class BoneAnimation {
	String boneName;
	KeyframeStack<Keyframe<IValue>> rotationKeyFrames;
	KeyframeStack<Keyframe<IValue>> positionKeyFrames;
	KeyframeStack<Keyframe<IValue>> scaleKeyFrames;

	/**
	 * A record of a deserialized animation for a given bone.<br>
	 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
	 * @param boneName The name of the bone as listed in the {@code animation.json}
	 * @param rotationKeyFrames The deserialized rotation {@code Keyframe} stack
	 * @param positionKeyFrames The deserialized position {@code Keyframe} stack
	 * @param scaleKeyFrames The deserialized scale {@code Keyframe} stack
	 */
	public BoneAnimation(String boneName,
						 KeyframeStack<Keyframe<IValue>> rotationKeyFrames,
						 KeyframeStack<Keyframe<IValue>> positionKeyFrames,
						 KeyframeStack<Keyframe<IValue>> scaleKeyFrames){
		this.boneName = boneName;
		this.rotationKeyFrames = rotationKeyFrames;
		this.positionKeyFrames = positionKeyFrames;
		this.scaleKeyFrames = scaleKeyFrames;

	}

	public String boneName() {
		return boneName;
	}

	public KeyframeStack<Keyframe<IValue>> rotationKeyFrames() {
		return rotationKeyFrames;
	}

	public KeyframeStack<Keyframe<IValue>> positionKeyFrames() {
		return positionKeyFrames;
	}

	public KeyframeStack<Keyframe<IValue>> scaleKeyFrames() {
		return scaleKeyFrames;
	}
}
