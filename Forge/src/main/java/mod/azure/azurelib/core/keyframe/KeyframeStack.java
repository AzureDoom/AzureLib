/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import java.util.List;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class KeyframeStack<T extends Keyframe> {
	/**
	 * The X key frames.
	 */
	public List<T> xKeyframes;
	/**
	 * The Y key frames.
	 */
	public List<T> yKeyframes;
	/**
	 * The Z key frames.
	 */
	public List<T> zKeyframes;

	/**
	 * Instantiates a new vector key frame list from 3 lists of keyframes
	 *
	 * @param XKeyFrames the x key frames
	 * @param YKeyFrames the y key frames
	 * @param ZKeyFrames the z key frames
	 */
	public KeyframeStack(List<T> XKeyFrames, List<T> YKeyFrames, List<T> ZKeyFrames) {
		xKeyframes = XKeyFrames;
		yKeyframes = YKeyFrames;
		zKeyframes = ZKeyFrames;
	}

	/**
	 * Instantiates a new blank key frame list
	 */
	public KeyframeStack() {
		xKeyframes = new ObjectArrayList<>();
		yKeyframes = new ObjectArrayList<>();
		zKeyframes = new ObjectArrayList<>();
	}

	public double getLastKeyframeTime() {
		double xTime = 0;
		for (T frame : xKeyframes) {
			xTime += frame.getLength();
		}

		double yTime = 0;
		for (T frame : yKeyframes) {
			yTime += frame.getLength();
		}

		double zTime = 0;
		for (T frame : zKeyframes) {
			zTime += frame.getLength();
		}

		return Math.max(xTime, Math.max(yTime, zTime));
	}

	public List<T> xKeyframes() {
		return xKeyframes;
	}

	public List<T> yKeyframes() {
		return yKeyframes;
	}

	public List<T> zKeyframes() {
		return zKeyframes;
	}
}
