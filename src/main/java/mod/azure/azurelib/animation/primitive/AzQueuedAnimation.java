package mod.azure.azurelib.animation.primitive;

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
 * </ul>
 */
public final class AzQueuedAnimation {

    private final AzBakedAnimation animation;

    private final AzPlayBehavior playBehavior;

    private final Boolean reverseOverride;

    public AzQueuedAnimation(AzBakedAnimation animation, AzPlayBehavior playBehavior, Boolean reverseOverride) {
        this.animation = animation;
        this.playBehavior = playBehavior;
        this.reverseOverride = reverseOverride;
    }

    public AzBakedAnimation animation() {
        return this.animation;
    }

    public AzPlayBehavior playBehavior() {
        return this.playBehavior;
    }

    public Boolean reverseOverride() {
        return this.reverseOverride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzQueuedAnimation))
            return false;
        AzQueuedAnimation other = (AzQueuedAnimation) o;
        return java.util.Objects.equals(this.animation, other.animation)
            && java.util.Objects.equals(this.playBehavior, other.playBehavior)
            && java.util.Objects.equals(this.reverseOverride, other.reverseOverride);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.animation);
        result = 31 * result + java.util.Objects.hashCode(this.playBehavior);
        result = 31 * result + java.util.Objects.hashCode(this.reverseOverride);
        return result;
    }

    @Override
    public String toString() {
        return "AzQueuedAnimation[animation=" + this.animation + ", playBehavior=" + this.playBehavior
            + ", reverseOverride=" + this.reverseOverride + "]";
    }

    public AzQueuedAnimation(AzBakedAnimation animation, AzPlayBehavior playBehavior) {
        this(animation, playBehavior, null);
    }
}
