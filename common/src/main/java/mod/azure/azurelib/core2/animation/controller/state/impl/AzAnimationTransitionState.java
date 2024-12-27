package mod.azure.azurelib.core2.animation.controller.state.impl;

import mod.azure.azurelib.core2.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.core2.animation.controller.state.machine.AzAnimationControllerStateMachine;

/**
 * Represents a transition state in an animation state machine. This state is responsible for managing the transition
 * between animations, including handling setup, updates, and transitioning to the appropriate play state when the
 * transition is complete. The `AzAnimationTransitionState` extends the functionality of `AzAnimationState` to implement
 * the behavior specific to transitioning between animations. It resets timers, initializes animations, and updates
 * keyframes to create smooth transitions.
 *
 * @param <T> the type of the animation context associated with this state
 */
public final class AzAnimationTransitionState<T> extends AzAnimationState<T> {

    public AzAnimationTransitionState() {}

    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        super.onEnter(context);
        prepareTransition(context);
    }

    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        var controller = context.animationController();
        var controllerTimer = controller.controllerTimer();
        var animContext = context.animationContext();

        var stateMachine = context.stateMachine();
        var boneCache = animContext.boneCache();

        var transitionLength = controller.animationProperties().transitionLength();
        var hasFinishedTransitioning = controllerTimer.getAdjustedTick() >= transitionLength;

        if (hasFinishedTransitioning) {
            // If we've exceeded the amount of time we should be transitioning, then switch to play state.
            stateMachine.play();
            return;
        }

        if (controller.currentAnimation() != null) {
            var bones = boneCache.getBakedModel().getBonesByName();
            var crashWhenCantFindBone = animContext.config().crashIfBoneMissing();
            var keyframeTransitioner = controller.keyframeManager().keyframeTransitioner();

            keyframeTransitioner.transition(bones, crashWhenCantFindBone, controllerTimer.getAdjustedTick());
        }
    }

    private void prepareTransition(AzAnimationControllerStateMachine.Context<?> context) {
        var animContext = context.animationContext();
        var boneCache = animContext.boneCache();
        var controller = context.animationController();
        var boneSnapshotCache = controller.boneSnapshotCache();
        var controllerTimer = controller.controllerTimer();

        controllerTimer.reset();
        controller.keyframeManager().keyframeCallbackHandler().reset();

        var nextAnimation = controller.animationQueue().next();

        if (nextAnimation == null) {
            return;
        }

        controller.setCurrentAnimation(nextAnimation);

        var snapshots = boneCache.getBoneSnapshotsByName();

        boneSnapshotCache.put(nextAnimation, snapshots.values());
    }
}
