package mod.azure.azurelib.util.state;

/**
 * Represents the contract for a state machine context. A state machine context serves as an intermediary object that
 * provides necessary data and functionality to state machine components or processes. It is generally used to
 * encapsulate and manage shared resources, state-related details, and other dependencies required during the lifecycle
 * of state transitions within a state machine. <br>
 * Implementations of this interface can define custom context-specific properties and methods, tailored to the
 * requirements of a specific state machine. <br>
 * The context is typically associated with the {@link State} and {@link StateMachine} interfaces. Implementing classes
 * provide relevant properties or methods that a state or state machine would require during its lifecycle operations
 * (e.g., onEnter, onUpdate, onExit for states).
 */
public interface StateMachineContext {}
