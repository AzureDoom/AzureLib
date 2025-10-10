package mod.azure.azurelib.animation.event;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframe;
import mod.azure.azurelib.core.keyframe.event.data.KeyFrameData;

/**
 * The base class for {@link AzKeyframe} events.<br>
 * These will be passed to one of the controllers in {@link AzAnimationController} when encountered during animation.
 *
 * @see AzCustomInstructionKeyframeEvent
 * @see AzParticleKeyframeEvent
 * @see AzSoundKeyframeEvent
 */
public abstract class AzKeyframeEvent<T, E extends KeyFrameData> {

    private final T animatable;

    private final double animationTick;

    private final AzAnimationController<T> controller;

    private final E eventKeyframe;

    protected AzKeyframeEvent(
        T animatable,
        double animationTick,
        AzAnimationController<T> controller,
        E eventKeyframe
    ) {
        this.animatable = animatable;
        this.animationTick = animationTick;
        this.controller = controller;
        this.eventKeyframe = eventKeyframe;
    }

    /**
     * Gets the amount of ticks that have passed in either the current transition or animation, depending on the
     * controller's AnimationState.
     */
    public double getAnimationTick() {
        return animationTick;
    }

    /**
     * Gets the {@link T animatable} object being rendered
     */
    public T getAnimatable() {
        return animatable;
    }

    /**
     * Gets the {@link AzAnimationController} responsible for the currently playing animation
     */
    public AzAnimationController<T> getController() {
        return controller;
    }

    /**
     * Returns the {@link KeyFrameData} relevant to the encountered {@link AzKeyframe}
     */
    public E getKeyframeData() {
        return this.eventKeyframe;
    }
}
