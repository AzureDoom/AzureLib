package mod.azure.azurelib.animation.primitive;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.animation.play_behavior.AzPlayBehavior;
import mod.azure.azurelib.animation.property.AzAnimationProperties;

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
 * <li>{@code stageProperties}: the stage's own playback settings (speed, transition length, easing, start offset,
 * freeze point, repeat count), layered over the controller's while this stage plays; {@code null} or unset values
 * inherit the controller's. Unlike controller-level settings, these never outlive the stage.</li>
 * </ul>
 */
public record AzQueuedAnimation(
    AzBakedAnimation animation,
    AzPlayBehavior playBehavior,
    @Nullable Boolean reverseOverride,
    @Nullable AzAnimationProperties stageProperties
) {

    public AzQueuedAnimation(AzBakedAnimation animation, AzPlayBehavior playBehavior) {
        this(animation, playBehavior, null, null);
    }

    public AzQueuedAnimation(
        AzBakedAnimation animation,
        AzPlayBehavior playBehavior,
        @Nullable Boolean reverseOverride
    ) {
        this(animation, playBehavior, reverseOverride, null);
    }
}
