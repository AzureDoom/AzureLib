/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
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

    /**
     * The keyframe's easing curve, resolved when the animation loaded, or {@code null} if it has to be resolved at
     * playback. Only valid for the keyframe's own easing type.
     */
    @Nullable
    Double2DoubleFunction bakedTransformer;

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
        this.bakedTransformer = null;
        return this;
    }

    /**
     * Overwrites all fields in-place, including the keyframe's baked easing curve.
     */
    public AzAnimationPoint set(
        @Nullable AzKeyframe<?> keyframe,
        double currentTick,
        double transitionLength,
        double animationStartValue,
        double animationEndValue,
        @Nullable Double2DoubleFunction bakedTransformer
    ) {
        set(keyframe, currentTick, transitionLength, animationStartValue, animationEndValue);
        this.bakedTransformer = bakedTransformer;
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

    /**
     * The keyframe's easing curve as resolved when the animation loaded (see
     * {@link mod.azure.azurelib.animation.easing.AzEasingType#resolveTransformer}), or {@code null} if it wasn't
     * resolved ahead of time. Only valid for {@link #keyframe()}'s own easing type.
     */
    @Nullable
    public Double2DoubleFunction bakedTransformer() {
        return bakedTransformer;
    }

    @Override
    public @NotNull String toString() {
        return "Tick: " + currentTick +
            " | Transition Length: " + transitionLength +
            " | Start Value: " + animationStartValue +
            " | End Value: " + animationEndValue;
    }
}
