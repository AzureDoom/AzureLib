/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.azure.azurelib.core.math.IValue;

import java.util.List;

/**
 * Stores a triplet of {@link Keyframe Keyframes} in an ordered stack
 */
public record KeyframeStack<T extends IValue>(List<Keyframe<T>> xKeyframes, List<Keyframe<T>> yKeyframes, List<Keyframe<T>> zKeyframes) {
	public KeyframeStack() {
		this(new ObjectArrayList<>(), new ObjectArrayList<>(), new ObjectArrayList<>());
	}

	public static <F extends IValue> KeyframeStack<F> from(KeyframeStack<F> otherStack) {
		return new KeyframeStack<>(otherStack.xKeyframes, otherStack.yKeyframes, otherStack.zKeyframes);
	}

	public double getLastKeyframeTime() {
		double xTime = 0;
		double yTime = 0;
		double zTime = 0;

		for (Keyframe<T> frame : xKeyframes()) {
			xTime += frame.length();
		}

		for (Keyframe<T> frame : yKeyframes()) {
			yTime += frame.length();
		}

		for (Keyframe<T> frame : zKeyframes()) {
			zTime += frame.length();
		}

		return Math.max(xTime, Math.max(yTime, zTime));
	}
}
