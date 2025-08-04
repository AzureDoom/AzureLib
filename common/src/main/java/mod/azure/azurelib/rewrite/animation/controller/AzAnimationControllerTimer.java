package mod.azure.azurelib.rewrite.animation.controller;

/**
 * A timer utility that integrates directly with an {@link AzAnimationController} to track and adjust tick values for
 * animation playback control, based on the controller's state and animation speed modifiers.
 *
 * @param <T> The type of the animatable entity being controlled by the animation controller.
 */
public class AzAnimationControllerTimer<T> {

    private AzAnimationController<T> animationController;

    private double adjustedTick;

    private double tickOffset;

    public AzAnimationControllerTimer(AzAnimationController<T> animationController) {
        this.animationController = animationController;
    }

    /**
     * Updates the internally tracked adjusted tick value for the animation timer. This method retrieves the current
     * animation time, combines it with relevant modifiers from the animation controller, such as animation speed and
     * start tick offset, and adjusts the tick value accordingly. The calculation incorporates the animation speed
     * multiplier to ensure proper playback rate scaling and offsets the calculation based on the tick offset.
     * <p>
     * The adjusted tick value is computed as: - Multiply the animation speed by the maximum of: - The difference
     * between the current animation time (plus start tick offset) and the existing tick offset. - The start tick
     * offset.
     */
    public void update() {
        var stateMachine = animationController.stateMachine();
        var animContext = stateMachine.getContext().animationContext();
        var animationSpeed = animationController.animationProperties().animationSpeed();
        var tick = animContext.timer().getAnimTime();
        double tickStartOffset = animationController.animationProperties().startTickOffset();

        adjustedTick = animationSpeed * Math.max((tick + tickStartOffset) - tickOffset, tickStartOffset);
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
