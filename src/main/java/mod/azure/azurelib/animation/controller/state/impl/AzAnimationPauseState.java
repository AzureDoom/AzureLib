package mod.azure.azurelib.animation.controller.state.impl;

import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;

/**
 * Represents the paused state in the animation state machine. This state ensures no updates are applied to the
 * animation while it is paused, maintaining its current state until it is transitioned back to a play or stop state.
 *
 * @param <T> the type of animation managed by the state
 */
public final class AzAnimationPauseState<T> extends AzAnimationPlayState<T> {

    public AzAnimationPauseState() {}

    /**
     * Handles the entry behavior for the pause state in the animation state machine. This method is invoked when the
     * state machine transitions into the pause state. No changes or resets are performed, as the pause state is meant
     * to preserve the current state of the animation without modification.
     *
     * @param context the context of the animation state machine, providing access to the animation controller,
     *                animation context, and state machine properties
     */
    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        // Do nothing, because the pause state shouldn't reset on enter.
    }

    /**
     * Updates the animation state when in the paused state. This method does not perform any animation updates or
     * transitions, and simply ensures that the paused state remains idle without altering animation behavior.
     *
     * @param context the current context of the animation state machine, containing the animation controller, context
     *                information, and state machine properties
     */
    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        super.onUpdate(context);
        // Pause state does not need to do anything.
    }
}
