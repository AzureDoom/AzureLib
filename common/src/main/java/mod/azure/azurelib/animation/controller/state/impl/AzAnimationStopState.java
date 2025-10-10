package mod.azure.azurelib.animation.controller.state.impl;

import mod.azure.azurelib.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;

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

    /**
     * Updates the "stop" state within the animation state machine. In this state, no actions are performed, as the stop
     * state is meant to represent an idle or inactive behavior where no animations are running. This method is a no-op,
     * providing a placeholder for any update-related logic, should it be introduced in the future.
     *
     * @param context the context of the animation state machine containing references to the animation controller,
     *                animation context, and state machine properties
     */
    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        // Stop state does not need to do anything.
    }
}
