package mod.azure.azurelib.animation.controller;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;

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
        AzAnimationControllerStateMachine<?> stateMachine = animationController.stateMachine();
        AzAnimationContext<?> animContext = stateMachine.getContext().animationContext();
        double animationSpeed = animationController.animationProperties().animationSpeed();
        double tick = animContext.timer().getAnimTime();
        double tickStartOffset = animationController.animationProperties().startTickOffset();
        double freezeTick = animationController.animationProperties().freezeTickOffset();

        if (freezeTick > 0 && adjustedTick >= freezeTick) {
            adjustedTick = freezeTick;
            return;
        }

        adjustedTick = animationSpeed * Math.max((tick + tickStartOffset) - tickOffset, tickStartOffset);
    }

    /**
     * Resets the internal state of the animation timer. This method updates the tick offset value to the current
     * animation time retrieved from the associated animation context's timer and resets the adjusted tick to zero. It
     * effectively synchronizes the timer with the current state of the animation controller, ensuring that subsequent
     * tick calculations reflect the reset starting point.
     */
    public void reset() {
        AzAnimationControllerStateMachine<?> stateMachine = animationController.stateMachine();
        AzAnimationContext<?> animContext = stateMachine.getContext().animationContext();
        this.tickOffset = animContext.timer().getAnimTime();
        this.adjustedTick = 0;
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
     * over time.
     *
     * @param adjustedTick The value to be added to the current-adjusted tick. This parameter represents the amount by
     *                     which the adjusted tick should be updated.
     */
    public void addToAdjustedTick(double adjustedTick) {
        this.adjustedTick += adjustedTick;
    }
}
