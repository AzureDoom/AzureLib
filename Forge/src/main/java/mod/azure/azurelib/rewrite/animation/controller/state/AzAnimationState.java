package mod.azure.azurelib.rewrite.animation.controller.state;

import mod.azure.azurelib.rewrite.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.rewrite.util.state.State;

/**
 * Represents an abstract animation state within the {@link AzAnimationControllerStateMachine}. Each concrete
 * implementation of this class defines specific behavior for managing animations during state transitions. <br/>
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

    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        this.isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void onExit(AzAnimationControllerStateMachine.Context<T> context) {
        this.isActive = false;
    }
}
