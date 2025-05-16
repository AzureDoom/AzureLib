package mod.azure.azurelib.rewrite.animation.controller.state.impl;

import mod.azure.azurelib.rewrite.animation.controller.state.machine.AzAnimationControllerStateMachine;

/**
 * Represents the paused state in the animation state machine. This state ensures no updates are applied to the
 * animation while it is paused, maintaining its current state until it is transitioned back to a play or stop state.
 *
 * @param <T> the type of animation managed by the state
 */
public final class AzAnimationPauseState<T> extends AzAnimationPlayState<T> {

    public AzAnimationPauseState() {}

    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        // Do nothing, because the pause state shouldn't reset on enter.
    }

    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        super.onUpdate(context);
        // Pause state does not need to do anything.
    }
}
