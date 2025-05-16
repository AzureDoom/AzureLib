package mod.azure.azurelib.rewrite.animation.controller.state.machine;

import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationPauseState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationPlayState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationStopState;
import mod.azure.azurelib.rewrite.animation.controller.state.impl.AzAnimationTransitionState;

public class StateHolder<T> {

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
        return playState;
    }

    public AzAnimationPauseState<T> pauseState() {
        return pauseState;
    }

    public AzAnimationStopState<T> stopState() {
        return stopState;
    }

    public AzAnimationTransitionState<T> transitionState() {
        return transitionState;
    }

    @Override
    public String toString() {
        return "StateHolder{" +
            "playState=" + playState +
            ", pauseState=" + pauseState +
            ", stopState=" + stopState +
            ", transitionState=" + transitionState +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StateHolder<?> that = (StateHolder<?>) o;

        if (!playState.equals(that.playState)) {
            return false;
        }
        if (!pauseState.equals(that.pauseState)) {
            return false;
        }
        if (!stopState.equals(that.stopState)) {
            return false;
        }
        return transitionState.equals(that.transitionState);
    }

    @Override
    public int hashCode() {
        int result = playState.hashCode();
        result = 31 * result + pauseState.hashCode();
        result = 31 * result + stopState.hashCode();
        result = 31 * result + transitionState.hashCode();
        return result;
    }
}
