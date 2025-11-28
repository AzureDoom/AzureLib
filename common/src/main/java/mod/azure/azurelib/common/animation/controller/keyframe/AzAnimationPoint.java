package mod.azure.azurelib.common.animation.controller.keyframe;

public final class AzAnimationPoint {

    private AzKeyframe<?> keyframe;

    private double currentTick;

    private double transitionLength;

    private double animationStartValue;

    private double animationEndValue;

    /**
     * Animation state record that holds the state of an animation at a given point
     *
     * @param currentTick         The lerped tick time (current tick + partial tick) of the point
     * @param transitionLength    The length of time (in ticks) that the point should take to transition
     * @param animationStartValue The start value to provide to the animation handling system
     * @param animationEndValue   The end value to provide to the animation handling system
     * @param keyframe            The {@code Nullable} Keyframe
     */
    public AzAnimationPoint(
        AzKeyframe<?> keyframe,
        double currentTick,
        double transitionLength,
        double animationStartValue,
        double animationEndValue
    ) {
        this.keyframe = keyframe;
        this.currentTick = currentTick;
        this.transitionLength = transitionLength;
        this.animationStartValue = animationStartValue;
        this.animationEndValue = animationEndValue;
    }

    public AzKeyframe<?> keyframe() {
        return keyframe;
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

    public void reinit(
        AzKeyframe<?> keyframe,
        double currentTick,
        double transitionLength,
        double animationStartValue,
        double animationEndValue
    ) {
        this.keyframe = keyframe;
        this.currentTick = currentTick;
        this.transitionLength = transitionLength;
        this.animationStartValue = animationStartValue;
        this.animationEndValue = animationEndValue;
    }

    public void reset() {
        this.keyframe = null;
        this.currentTick = 0;
        this.transitionLength = 0;
        this.animationStartValue = 0;
        this.animationEndValue = 0;
    }

    @Override
    public String toString() {
        return "Tick: " + this.currentTick +
            " | Transition Length: " + this.transitionLength +
            " | Start Value: " + this.animationStartValue +
            " | End Value: " + this.animationEndValue;
    }
}
