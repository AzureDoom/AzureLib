package mod.azure.azurelib.rewrite.animation.controller;

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

    /**
     * Adjust a tick value depending on the controller's current state and speed modifier.<br>
     * Is used when starting a new animation, transitioning, and a few other key areas
     */
    public void update() {
        var stateMachine = animationController.stateMachine();
        var animContext = stateMachine.getContext().animationContext();
        var animationSpeed = animationController.animationProperties().animationSpeed();
        var tick = animContext.timer().getAnimTime();

        adjustedTick = animationSpeed * Math.max(tick - tickOffset, 0);
    }

    public void reset() {
        var stateMachine = animationController.stateMachine();
        var animContext = stateMachine.getContext().animationContext();
        this.tickOffset = animContext.timer().getAnimTime();
        this.adjustedTick = 0;
    }

    public double getAdjustedTick() {
        return adjustedTick;
    }

    public void addToAdjustedTick(double adjustedTick) {
        this.adjustedTick += adjustedTick;
    }
}
