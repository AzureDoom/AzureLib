/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import com.eliotlash.mclib.math.IValue;

public class BoneAnimation {
	public final String boneName;

	public KeyframeStack<Keyframe<IValue>> rotationKeyFrames;
	public KeyframeStack<Keyframe<IValue>> positionKeyFrames;
	public KeyframeStack<Keyframe<IValue>> scaleKeyFrames;
	
	public BoneAnimation(String boneName, KeyframeStack<Keyframe<IValue>> rotationKeyFrames, KeyframeStack<Keyframe<IValue>> positionkeyFrames, KeyframeStack<Keyframe<IValue>> scaleKeyFrames) {
		this.boneName = boneName;
		this.rotationKeyFrames = rotationKeyFrames;
		this.positionKeyFrames = positionkeyFrames;
		this.scaleKeyFrames = scaleKeyFrames;
	}

	public BoneAnimation(String boneName) {
		this.boneName = boneName;
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
