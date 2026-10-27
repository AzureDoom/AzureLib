package mod.azure.azurelib.common.animation.controller;

import mod.azure.azurelib.common.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.common.animation.property.AzAnimationProperties;

/**
 * A timer utility that integrates directly with an {@link AzAnimationController} to track and adjust tick values for
 * animation playback control, based on the controller's state and animation speed modifiers.
 *
 * @param <T> The type of the animatable entity being controlled by the animation controller.
 */
public class AzAnimationControllerTimer<T> {

    private final AzAnimationController<T> animationController;

    private double adjustedTick;

    private double tickOffset;

    public AzAnimationControllerTimer(AzAnimationController<T> animationController) {
        this.animationController = animationController;
    }

    public void update() {
        AzAnimationControllerStateMachine<T> stateMachine = animationController.stateMachine();
        AzAnimationProperties properties = animationController.effectiveProperties();
        double animationSpeed = properties.animationSpeed();
        double elapsed = animTime() - tickOffset;

        if (stateMachine.isTransitioning()) {
            adjustedTick = Math.max(0, animationSpeed * elapsed);
            return;
        }

        double progress = Math.max(0, properties.startTickOffset() + animationSpeed * elapsed);
        double freezeTick = animationController.effectiveFreezeTickOffset();

        if (freezeTick > 0 && progress > freezeTick) {
            progress = freezeTick;
        }

        adjustedTick = progress;
    }

    /**
     * Restarts elapsed time from the animator's current clock. While transitioning that puts {@link #getAdjustedTick()}
     * at 0; otherwise at the start tick offset, so the first frame of an animation (and of every loop, ping-pong leg or
     * repeat) already samples the right pose instead of frame 0.
     */
    public void reset() {
        this.tickOffset = animTime();

        if (animationController.stateMachine().isTransitioning()) {
            this.adjustedTick = 0;
        } else {
            this.adjustedTick = Math.max(0, animationController.effectiveProperties().startTickOffset());
        }
    }

    /**
     * Moves playback progress to {@code progress} in a way that survives the next {@link #update()}, by solving
     * {@code update()}'s formula for the tick offset. Unlike {@link #addToAdjustedTick(double)}, which is overwritten
     * on the next update, this persists.
     *
     * @param progress the adjusted tick to continue from
     */
    public void seek(double progress) {
        var properties = animationController.effectiveProperties();
        var speed = properties.animationSpeed();

        if (speed != 0) {
            var origin = animationController.stateMachine().isTransitioning() ? 0 : properties.startTickOffset();
            this.tickOffset = animTime() - (progress - origin) / speed;
        }

        this.adjustedTick = progress;
    }

    /**
     * Retrieves the adjusted tick value for the animation timer. The adjusted tick represents the calculated
     * progression of the animation timer, accounting for modifiers such as animation speed and tick offset.
     *
     * @return The current adjusted tick value as a double.
     */
    public double getAdjustedTick() {
        return adjustedTick;
    }

    /**
     * Adds the specified value to the currently tracked adjusted tick value for the animation controller timer. This
     * method increments the adjusted tick by the provided amount, allowing for cumulative adjustments to the tick value
     * over time. The change only lasts until the next {@link #update()}; use {@link #seek(double)} to move playback.
     *
     * @param adjustedTick The value to be added to the current-adjusted tick.
     */
    public void addToAdjustedTick(double adjustedTick) {
        this.adjustedTick += adjustedTick;
    }

    private double animTime() {
        return animationController.stateMachine().getContext().animationContext().timer().getAnimTime();
    }
}
