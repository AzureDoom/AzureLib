package mod.azure.azurelib.rewrite.animation.controller.state.impl;

import mod.azure.azurelib.rewrite.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.rewrite.animation.controller.state.machine.AzAnimationControllerStateMachine;

/**
 * Represents the "stop" state in an animation state machine. This state is responsible for halting any ongoing
 * animations and putting the animation controller into a minimal responsibility state where no further updates or
 * actions are performed until a new state transition occurs. <br/>
 * <br/>
 * This state is typically used when an animation sequence has fully completed and no <br/>
 * <br/>
 * Inherits the general animation state behavior and lifecycle from {@link AzAnimationState}.
 *
 * @param <T> the type of animation context associated with the state machine
 */
public final class AzAnimationStopState<T> extends AzAnimationState<T> {

    public AzAnimationStopState() {}

    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        // Stop state does not need to do anything.
    }
}
