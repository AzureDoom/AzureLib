package mod.azure.azurelib.animation.controller.state.machine;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationPauseState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationPlayState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationStopState;
import mod.azure.azurelib.animation.controller.state.impl.AzAnimationTransitionState;
import mod.azure.azurelib.util.state.StateMachine;
import mod.azure.azurelib.util.state.StateMachineContext;

/**
 * A state machine for managing animation controller states, providing functionality to transition between play, pause,
 * stop, and transition states. It is generic and supports handling context and states specific to animations.
 *
 * @param <T> the type of the animation the state machine controls
 */
public class AzAnimationControllerStateMachine<T> extends StateMachine<AzAnimationControllerStateMachine.Context<T>, AzAnimationState<T>> {

    private final StateHolder<T> stateHolder;

    public AzAnimationControllerStateMachine(
        StateHolder<T> stateHolder,
        AzAnimationController<T> animationController,
        AzAnimationContext<T> animationContext
    ) {
        super(stateHolder.stopState());
        this.stateHolder = stateHolder;
        getContext().stateMachine = this;
        getContext().animationController = animationController;
        getContext().animationContext = animationContext;
    }

    @Override
    public Context<T> createContext() {
        return new Context<>();
    }

    public void initializeContext(AzAnimationController<T> controller, AzAnimationContext<T> animContext) {
        AzAnimationControllerStateMachine.Context<T> ctx = getContext();
        ctx.animationController = controller;
        ctx.animationContext = animContext;
        ctx.stateMachine = this;
    }

    public void update() {
        super.update(getContext());
    }

    public void pause() {
        setState(stateHolder.pauseState);
    }

    public void play() {
        setState(stateHolder.playState);
    }

    public void transition() {
        setState(stateHolder.transitionState);
    }

    public void stop() {
        setState(stateHolder.stopState);
    }

    public boolean isPlaying() {
        return getState() == stateHolder.playState;
    }

    public boolean isPaused() {
        return getState() == stateHolder.pauseState;
    }

    public boolean isStopped() {
        return getState() == stateHolder.stopState;
    }

    public boolean isTransitioning() {
        return getState() == stateHolder.transitionState;
    }

    public static final class StateHolder<T> {

        private final AzAnimationPlayState<T> playState;

        private final AzAnimationPauseState<T> pauseState;

        private final AzAnimationStopState<T> stopState;

        private final AzAnimationTransitionState<T> transitionState;

        public StateHolder(
            AzAnimationPlayState<T> playState,
            AzAnimationPauseState<T> pauseState,
            AzAnimationStopState<T> stopState,
            AzAnimationTransitionState<T> transitionState
        ) {
            this.playState = playState;
            this.pauseState = pauseState;
            this.stopState = stopState;
            this.transitionState = transitionState;
        }

        public AzAnimationPlayState<T> playState() {
            return this.playState;
        }

        public AzAnimationPauseState<T> pauseState() {
            return this.pauseState;
        }

        public AzAnimationStopState<T> stopState() {
            return this.stopState;
        }

        public AzAnimationTransitionState<T> transitionState() {
            return this.transitionState;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof StateHolder))
                return false;
            StateHolder<?> other = (StateHolder<?>) o;
            return java.util.Objects.equals(this.playState, other.playState)
                && java.util.Objects.equals(this.pauseState, other.pauseState)
                && java.util.Objects.equals(this.stopState, other.stopState)
                && java.util.Objects.equals(this.transitionState, other.transitionState);
        }

        @Override
        public int hashCode() {
            int result = 0;
            result = 31 * result + java.util.Objects.hashCode(this.playState);
            result = 31 * result + java.util.Objects.hashCode(this.pauseState);
            result = 31 * result + java.util.Objects.hashCode(this.stopState);
            result = 31 * result + java.util.Objects.hashCode(this.transitionState);
            return result;
        }

        @Override
        public String toString() {
            return "StateHolder[playState=" + this.playState + ", pauseState=" + this.pauseState + ", stopState="
                + this.stopState + ", transitionState=" + this.transitionState + "]";
        }
    }

    public static class Context<T> implements StateMachineContext {

        private AzAnimationContext<T> animationContext;

        private AzAnimationController<T> animationController;

        private AzAnimationControllerStateMachine<T> stateMachine;

        private Context() {}

        public AzAnimationContext<T> animationContext() {
            return animationContext;
        }

        public AzAnimationController<T> animationController() {
            return animationController;
        }

        public AzAnimationControllerStateMachine<T> stateMachine() {
            return stateMachine;
        }
    }
}
