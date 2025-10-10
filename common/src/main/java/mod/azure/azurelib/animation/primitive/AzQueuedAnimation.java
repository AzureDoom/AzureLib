package mod.azure.azurelib.animation.primitive;

import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;

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
 * </ul>
 */
public record AzQueuedAnimation(
    AzBakedAnimation animation,
    AzPlayBehavior playBehavior
) {}
