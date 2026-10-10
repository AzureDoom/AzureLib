/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingTypes;
import mod.azure.azurelib.core.math.IValue;

/**
 * Animation keyframe data
 *
 * @param length     The length (in ticks) the keyframe lasts for
 * @param startValue The value to start the keyframe's transformation with
 * @param endValue   The value to end the keyframe's transformation with
 * @param easingType The {@code EasingType} to use for transformations
 * @param easingArgs The arguments to provide to the easing calculation
 */
public final class AzKeyframe<T extends IValue> {

    private final double length;

    private final T startValue;

    private final T endValue;

    private final AzEasingType easingType;

    private final List<T> easingArgs;

    public AzKeyframe(double length, T startValue, T endValue, AzEasingType easingType, List<T> easingArgs) {
        this.length = length;
        this.startValue = startValue;
        this.endValue = endValue;
        this.easingType = easingType;
        this.easingArgs = easingArgs;
    }

    public double length() {
        return this.length;
    }

    public T startValue() {
        return this.startValue;
    }

    public T endValue() {
        return this.endValue;
    }

    public AzEasingType easingType() {
        return this.easingType;
    }

    public List<T> easingArgs() {
        return this.easingArgs;
    }

    @Override
    public String toString() {
        return "AzKeyframe[length=" + this.length + ", startValue=" + this.startValue + ", endValue=" + this.endValue
            + ", easingType=" + this.easingType + ", easingArgs=" + this.easingArgs + "]";
    }

    public AzKeyframe(double length, T startValue, T endValue) {
        this(length, startValue, endValue, AzEasingTypes.LINEAR);
    }

    public AzKeyframe(double length, T startValue, T endValue, AzEasingType easingType) {
        this(length, startValue, endValue, easingType, new ArrayList<>(0));
    }

    @Override
    public int hashCode() {
        int result = 31 + Double.hashCode(this.length);
        result = 31 * result + Objects.hashCode(this.startValue);
        result = 31 * result + Objects.hashCode(this.endValue);
        result = 31 * result + Objects.hashCode(this.easingType);
        result = 31 * result + Objects.hashCode(this.easingArgs);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (!(obj instanceof AzKeyframe))
            return false;

        return Double.compare(this.length, ((AzKeyframe<?>) obj).length()) == 0
            && Objects.equals(this.startValue, ((AzKeyframe<?>) obj).startValue())
            && Objects.equals(this.endValue, ((AzKeyframe<?>) obj).endValue())
            && Objects.equals(this.easingType, ((AzKeyframe<?>) obj).easingType())
            && Objects.equals(this.easingArgs, ((AzKeyframe<?>) obj).easingArgs());
    }
}
