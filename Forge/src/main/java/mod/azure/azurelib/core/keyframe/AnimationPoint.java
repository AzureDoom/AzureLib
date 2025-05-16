/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

/**
 * Animation state record that holds the state of an animation at a given point
 *
 * @param currentTick         The lerped tick time (current tick + partial tick) of the point
 * @param transitionLength    The length of time (in ticks) that the point should take to transition
 * @param animationStartValue The start value to provide to the animation handling system
 * @param animationEndValue   The end value to provide to the animation handling system
 * @param keyFrame            The {@code Nullable} Keyframe
 */
@Deprecated()
public class AnimationPoint {

    private Keyframe<?> keyFrame;

    private double currentTick;

    private double transitionLength;

    private double animationStartValue;

    private double animationEndValue;

    public AnimationPoint(
        Keyframe<?> keyFrame,
        double currentTick,
        double transitionLength,
        double animationStartValue,
        double animationEndValue
    ) {
        this.keyFrame = keyFrame;
        this.currentTick = currentTick;
        this.transitionLength = transitionLength;
        this.animationStartValue = animationStartValue;
        this.animationEndValue = animationEndValue;
    }

    @Override
    public String toString() {
        return "Tick: " + currentTick +
            " | Transition Length: " + transitionLength +
            " | Start Value: " + animationStartValue +
            " | End Value: " + animationEndValue;
    }

    public Keyframe<?> keyFrame() {
        return keyFrame;
    }

    public double currentTick() {
        return currentTick;
    }

    public double transitionLength() {
        return transitionLength;
    }

    public double animationStartValue() {
        return animationStartValue;
    }

    public double animationEndValue() {
        return animationEndValue;
    }
}
