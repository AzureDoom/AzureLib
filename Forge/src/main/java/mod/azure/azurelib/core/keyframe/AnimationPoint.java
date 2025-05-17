/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe;

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
