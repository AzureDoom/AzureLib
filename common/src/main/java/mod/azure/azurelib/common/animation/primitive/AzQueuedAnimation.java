package mod.azure.azurelib.common.animation.primitive;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.common.animation.property.AzAnimationProperties;

/**
 * Represents an entry in an animation queue, combining an animation and its looping behavior. This record defines a
 * queued animation to be played, including its associated {@link AzBakedAnimation} instance and the
 * {@link AzPlayBehavior} that determines how the animation behaves once it reaches the end of its sequence. <br/>
 * <br/>
 * Instances of AzQueuedAnimation are immutable by design, ensuring that queued animations, once defined, cannot be
 * modified, preserving their behavior within the animation controller. <br/>
 * <br/>
 * Fields:
 * <ul>
 * <li>{@code animation}: The {@link AzBakedAnimation} instance that contains the actual animation data to be
 * played.</li>
 * <li>{@code playBehavior}: The {@link AzPlayBehavior} that dictates the looping behavior or termination handling for
 * the animation.</li>
 * <li>{@code reverseOverride}: the stage's own playback direction, or {@code null} to inherit the controller's
 * {@link AzAnimationProperties#isReversing()}.</li>
 * </ul>
 */
public record AzQueuedAnimation(
    AzBakedAnimation animation,
    AzPlayBehavior playBehavior,
    @Nullable Boolean reverseOverride
) {

    public AzQueuedAnimation(AzBakedAnimation animation, AzPlayBehavior playBehavior) {
        this(animation, playBehavior, null);
    }
}
