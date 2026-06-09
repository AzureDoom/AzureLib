/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Mutable animation state holder used to describe the state of an animation at a given point. Previously a record;
 * converted to a mutable class so instances can be pooled inside {@link AzBoneAnimationQueue} and reused every frame
 * without heap allocation.
 * <p>
 * All fields are package-accessible for pool writes; external code uses the getters.
 * </p>
 */
public final class AzAnimationPoint {

    @Nullable
    AzKeyframe<?> keyframe;

    double currentTick;

    double transitionLength;

    double animationStartValue;

    double animationEndValue;

    AzAnimationPoint() {}

    public AzAnimationPoint(
        @Nullable AzKeyframe<?> keyframe,
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

    /**
     * Overwrites all fields in-place. Used by the pool to recycle instances each frame.
     */
    public AzAnimationPoint set(
        @Nullable AzKeyframe<?> keyframe,
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
        return this;
    }

    @Nullable
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
    public @NotNull String toString() {
        return "Tick: " + currentTick +
            " | Transition Length: " + transitionLength +
            " | Start Value: " + animationStartValue +
            " | End Value: " + animationEndValue;
    }
}
