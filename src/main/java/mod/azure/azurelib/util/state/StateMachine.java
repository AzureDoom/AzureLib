package mod.azure.azurelib.util.state;

/**
 * Represents a state machine that handles transitions between different states. A state machine maintains a current
 * state and allows transitions to new states, invoking lifecycle methods on each state during transitions.
 *
 * @param <C> the type of the context associated with the state machine, which extends {@link StateMachineContext}
 * @param <T> the type of the states managed by the state machine, which extends {@link State}
 */
public abstract class StateMachine<C extends StateMachineContext, T extends State<C>> {

    private final C reusableContext;

    private T state;

    public StateMachine(T initialState) {
        this.state = initialState;
        this.reusableContext = createContext();
    }

    protected abstract C createContext();

    public void update(C context) {
        state.onUpdate(context);
    }

    public C getContext() {
        return reusableContext;
    }

    public T getState() {
        return state;
    }

    public void setState(T newState) {
        state.onExit(reusableContext);
        this.state = newState;
        newState.onEnter(reusableContext);
    }
}
