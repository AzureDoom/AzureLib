package mod.azure.azurelib.util.state;

/**
 * Represents a state within a state machine. A state defines the behavior that occurs when entering, updating, and
 * exiting the state. The lifecycle of a state consists of three main methods: <br>
 * <ol>
 * <li><b>onEnter</b> - Triggered when the state is entered, providing an opportunity to perform initialization.</li>
 * <li><b>onUpdate</b> - Called during state execution, typically to update or process logic related to the state.</li>
 * <li><b>onExit</b> - Triggered when transitioning out of the state, used for cleanup or finalization.</li>
 * </ol>
 *
 * @param <C> the type of the context associated with the state, which must extend {@link StateMachineContext}
 */
public interface State<C extends StateMachineContext> {

    void onEnter(C context);

    void onUpdate(C context);

    void onExit(C context);
}
