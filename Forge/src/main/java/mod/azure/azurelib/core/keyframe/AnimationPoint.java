/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

public class AnimationPoint {
	/**
	 * The current tick in the animation to lerp from
	 */
	public final double currentTick;
	/**
	 * The tick that the current animation should end at
	 */
	public final double transitionLength;
	/**
	 * The Animation start value.
	 */
	public final double animationStartValue;
	/**
	 * The Animation end value.
	 */
	public final double animationEndValue;

	/**
	 * The current keyframe.
	 */
	public final Keyframe keyframe;

	public AnimationPoint(Keyframe keyFrame, double currentTick, double transitionLength, double animationStartValue, double animationEndValue) {
		this.keyframe = keyFrame;
		this.currentTick = currentTick;
		this.transitionLength = transitionLength;
		this.animationStartValue = animationStartValue;
		this.animationEndValue = animationEndValue;
	}

	public double animationStartValue() {
		return animationStartValue;
	}

	public double animationEndValue() {
		return animationEndValue;
	}

	public double transitionLength() {
		return transitionLength;
	}

	public double currentTick() {
		return currentTick;
	}

	@Override
	public String toString() {
		return "Tick: " + this.currentTick + " | Transition Length: " + this.transitionLength + " | Start Value: " + this.animationStartValue + " | End Value: " + this.animationEndValue;
	}

	public Keyframe keyFrame() {
		return keyframe;
	}
}
