package mod.azure.azurelib.animation.controller.state;

import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.util.state.State;

/**
 * Represents an abstract animation state within the {@link AzAnimationControllerStateMachine}. Each concrete
 * implementation of this class defines a specific behavior for managing animations during state transitions. <br/>
 * <br/>
 * The animation state lifecycle consists of three primary methods:
 * <ul>
 * <li>{@code onEnter}: Invoked when the state is entered. This method is used for initializing the state.</li>
 * <li>{@code onUpdate}: Should be implemented by subclasses to define the behavior during the state's execution.</li>
 * <li>{@code onExit}: Invoked when transitioning out of the state. This method is used for cleanup or
 * finalization.</li>
 * </ul>
 *
 * @param <T> the type of the animation context associated with this state
 */
public abstract class AzAnimationState<T> implements State<AzAnimationControllerStateMachine.Context<T>> {

    private boolean isActive;

    protected AzAnimationState() {
        this.isActive = false;
    }

    /**
     * Invoked when the state is entered in the animation state machine. This method is responsible for initializing the
     * state and marking it as active. Subclasses may override this method to define additional setup logic specific to
     * the state being entered.
     *
     * @param context the context associated with the state machine, providing access to the animation controller,
     *                animation context, and references to the state machine
     */
    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        this.isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Handles the exit operations when this animation state is transitioned out of. This method is invoked as part of
     * the state's lifecycle, specifically during the transition from the current state to another state in the
     * animation controller's state machine. It deactivates the current state by setting its active status to false.
     *
     * @param context the context associated with the state machine, which provides access to the animation controller,
     *                animation context, and state machine itself
     */
    @Override
    public void onExit(AzAnimationControllerStateMachine.Context<T> context) {
        this.isActive = false;
    }
}
