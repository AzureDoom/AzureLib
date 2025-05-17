/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.core.math.IValue;

public class AzKeyframe<T extends IValue> {

    private final double length;

    private final T startValue;

    private final T endValue;

    private final AzEasingType easingType;

    private final List<T> easingArgs;

    /**
     * Animation keyframe data
     *
     * @param length     The length (in ticks) the keyframe lasts for
     * @param startValue The value to start the keyframe's transformation with
     * @param endValue   The value to end the keyframe's transformation with
     */
    public AzKeyframe(double length, T startValue, T endValue) {
        this(length, startValue, endValue, AzEasingTypes.LINEAR);
    }

    /**
     * Animation keyframe data
     *
     * @param length     The length (in ticks) the keyframe lasts for
     * @param startValue The value to start the keyframe's transformation with
     * @param endValue   The value to end the keyframe's transformation with
     * @param easingType The {@code EasingType} to use for transformations
     */
    public AzKeyframe(double length, T startValue, T endValue, AzEasingType easingType) {
        this(length, startValue, endValue, easingType, new ObjectArrayList<>(0));
    }

    /**
     * Animation keyframe data
     *
     * @param length     The length (in ticks) the keyframe lasts for
     * @param startValue The value to start the keyframe's transformation with
     * @param endValue   The value to end the keyframe's transformation with
     * @param easingType The {@code EasingType} to use for transformations
     * @param easingArgs The arguments to provide to the easing calculation
     */
    public AzKeyframe(double length, T startValue, T endValue, AzEasingType easingType, List<T> easingArgs) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.easingArgs = easingArgs;
    }

    public double length() {
        return length;
    }

    public T startValue() {
        return startValue;
    }

    public T endValue() {
        return endValue;
    }

    public AzEasingType easingType() {
        return easingType;
    }

    public List<T> easingArgs() {
        return easingArgs;
    }

    @Override
    public String toString() {
        return "AzKeyframe{" +
            "length=" + length +
            ", startValue=" + startValue +
            ", endValue=" + endValue +
            ", easingType=" + easingType +
            ", easingArgs=" + easingArgs +
            '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.length, this.startValue, this.endValue, this.easingType, this.easingArgs);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AzKeyframe<?> other = (AzKeyframe<?>) obj;
        return Double.compare(this.length, other.length) == 0 &&
            Objects.equals(this.startValue, other.startValue) &&
            Objects.equals(this.endValue, other.endValue) &&
            Objects.equals(this.easingType, other.easingType) &&
            Objects.equals(this.easingArgs, other.easingArgs);
    }
}
