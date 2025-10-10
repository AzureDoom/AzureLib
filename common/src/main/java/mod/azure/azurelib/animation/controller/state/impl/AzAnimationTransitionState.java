package mod.azure.azurelib.animation.controller.state.impl;

import mod.azure.azurelib.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;

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

    /**
     * Invoked when the transition state is entered in the animation state machine. This method performs the necessary
     * setup for transitioning between animations, including calling the superclass's `onEnter` method and invoking the
     * `prepareTransition` method to initialize the transition process.
     *
     * @param context the context of the animation state machine, providing access to the animation controller,
     *                animation context, and references to the state machine
     */
    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        super.onEnter(context);
        prepareTransition(context);
    }

    /**
     * Updates the transition state of the animation controller. This method checks whether the transition period has
     * been completed and determines the appropriate next action, such as transitioning to the play state or continuing
     * the transition by updating keyframes.
     *
     * @param context The context of the animation controller state machine, providing the current animation controller,
     *                animation context, and state machine associated with this update.
     */
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

    /**
     * Prepares the animation transition process by resetting the necessary components, updating callbacks, caching bone
     * snapshots, and setting the next animation in the animation controller.
     *
     * @param context The context of the animation controller state machine, providing the animation context, animation
     *                controller, and references required for processing the transition.
     */
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
