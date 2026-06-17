package mod.azure.azurelib.animation.controller;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.property.AzAnimationProperties;

/**
 * A builder class to construct {@link AzAnimationController} instances for managing animations in {@link AzAnimator}.
 * This provides a fluent API to configure properties such as animation speed, keyframe callbacks, easing type,
 * transition length, and triggerable animations.
 *
 * @param <T> The type of object that the animation controller will operate on.
 */
public class AzAnimationControllerBuilder<T> {

    private final AzAnimator<?, T> animator;

    private final String name;

    private AzAnimationProperties animationProperties;

    private AzKeyframeCallbacks<T> keyframeCallbacks;

    public AzAnimationControllerBuilder(AzAnimator<?, T> animator, String name) {
        this.animator = animator;
        this.name = name;
        this.animationProperties = AzAnimationProperties.DEFAULT;
        this.keyframeCallbacks = AzKeyframeCallbacks.noop();
    }

    /**
     * Sets the speed of the animation for this controller. The animation speed determines how fast the animation
     * progresses relative to its default playback rate.
     *
     * @param animationSpeed The speed multiplier for the animation. A value of 1.0 plays the animation at its normal
     *                       speed, values greater than 1.0 play it faster, and values between 0.0 and 1.0 play it
     *                       slower.
     * @return The current instance of {@code AzAnimationControllerBuilder}, allowing for method chaining.
     */
    public AzAnimationControllerBuilder<T> setAnimationSpeed(double animationSpeed) {
        animationProperties = animationProperties.withAnimationSpeed(animationSpeed);
        return this;
    }

    /**
     * Sets the keyframe callbacks for this animation controller. The keyframe callbacks define how specific events,
     * such as sound or particle effects and custom instructions, are handled during the animation sequence.
     *
     * @param keyframeCallbacks The {@link AzKeyframeCallbacks} instance to be used for handling keyframe events. Must
     *                          not be null.
     * @return The current instance of {@code AzAnimationControllerBuilder}, enabling method chaining.
     */
    public AzAnimationControllerBuilder<T> setKeyframeCallbacks(@NotNull AzKeyframeCallbacks<T> keyframeCallbacks) {
        Objects.requireNonNull(keyframeCallbacks);
        this.keyframeCallbacks = keyframeCallbacks;
        return this;
    }

    /**
     * Sets the easing type for the animation in this builder. The easing type defines how the animation interpolates
     * between keyframes, affecting the timing and flow of the animation (e.g., linear, ease-in, ease-out, etc.).
     *
     * @param easingType The {@link AzEasingType} to be used for the animation's easing. This parameter must not be
     *                   null.
     * @return The current instance of {@code AzAnimationControllerBuilder}, enabling method chaining.
     */
    public AzAnimationControllerBuilder<T> setEasingType(AzEasingType easingType) {
        animationProperties = animationProperties.withEasingType(easingType);
        return this;
    }

    /**
     * Sets the transition length for the animation. The transition length determines the duration of the transition
     * between animation states, impacting how smoothly and gradually the change occurs.
     *
     * @param transitionLength The desired transition length, in ticks, to be set for the animation. It must be a
     *                         non-negative integer.
     * @return The current instance of {@code AzAnimationControllerBuilder}, allowing for method chaining.
     */
    public AzAnimationControllerBuilder<T> setTransitionLength(int transitionLength) {
        animationProperties = animationProperties.withTransitionLength(transitionLength);
        return this;
    }

    /**
     * Sets the start tick offset for the animation. The start tick offset determines the initial position within the
     * animation timeline, allowing the animation to start from a specific tick rather than from the beginning.
     *
     * @param startTickOffset The offset in ticks to set as the starting point of the animation. Must be a non-negative
     *                        value.
     * @return The current instance of {@code AzAnimationControllerBuilder}, allowing for method chaining.
     */
    public AzAnimationControllerBuilder<T> setStartTickOffset(double startTickOffset) {
        animationProperties = animationProperties.withStartTickOffset(startTickOffset);
        return this;
    }

    /**
     * Builds and returns a new instance of {@code AzAnimationController<T>} based on the parameters and configuration
     * of the current {@code AzAnimationControllerBuilder<T>} instance.
     *
     * @return A new {@code AzAnimationController<T>} instance, initialized with the specified animator, name, animation
     *         properties, and keyframe callbacks.
     */
    public AzAnimationController<T> build() {
        return new AzAnimationController<>(
            name,
            animator,
            animationProperties,
            keyframeCallbacks
        );
    }
}
