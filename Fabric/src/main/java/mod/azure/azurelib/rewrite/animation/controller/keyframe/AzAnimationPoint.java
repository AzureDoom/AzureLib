package mod.azure.azurelib.rewrite.animation.controller.keyframe;

/**
 * Animation state class that holds the state of an animation at a given point.
 *
 * Represents:
 * - A specific animation tick
 * - Transition duration
 * - Animation start and end values
 * - Associated keyframe (optional)
 */
public class AzAnimationPoint {
    private final AzKeyframe<?> keyframe;
    private final double currentTick;
    private final double transitionLength;
    private final double animationStartValue;
    private final double animationEndValue;

    public AzAnimationPoint(AzKeyframe<?> keyframe,
                            double currentTick,
                            double transitionLength,
                            double animationStartValue,
                            double animationEndValue) {
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

    @Override
    public String toString() {
        return "Tick: " + this.currentTick +
               " | Transition Length: " + this.transitionLength +
               " | Start Value: " + this.animationStartValue +
               " | End Value: " + this.animationEndValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AzAnimationPoint that = (AzAnimationPoint) o;

        if (Double.compare(that.currentTick, currentTick) != 0) return false;
        if (Double.compare(that.transitionLength, transitionLength) != 0) return false;
        if (Double.compare(that.animationStartValue, animationStartValue) != 0) return false;
        if (Double.compare(that.animationEndValue, animationEndValue) != 0) return false;
        return keyframe != null ? keyframe.equals(that.keyframe) : that.keyframe == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = keyframe != null ? keyframe.hashCode() : 0;
        temp = Double.doubleToLongBits(currentTick);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(transitionLength);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(animationStartValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(animationEndValue);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}