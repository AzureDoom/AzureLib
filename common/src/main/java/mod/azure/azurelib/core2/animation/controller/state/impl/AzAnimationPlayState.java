package mod.azure.azurelib.core2.animation.controller.state.impl;

import mod.azure.azurelib.core2.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.core2.animation.controller.state.machine.AzAnimationControllerStateMachine;

/**
 * Represents a "play" state in an animation state machine. This state is responsible for managing the playing of
 * animations either by starting from the beginning or playing subsequent animations. It ensures that the animation
 * progresses based on the controller's timer and handles transitions when animations complete. <br/>
 * <br/>
 * Inherits general animation state behavior such as lifecycle management from {@link AzAnimationState}.
 *
 * @param <T> the type of animation being managed
 */
public class AzAnimationPlayState<T> extends AzAnimationState<T> {

    public AzAnimationPlayState() {}

    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        super.onEnter(context);
        var controller = context.animationController();
        var controllerTimer = controller.controllerTimer();

        controllerTimer.reset();
    }

    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        var controller = context.animationController();
        var controllerTimer = controller.controllerTimer();
        var currentAnimation = controller.currentAnimation();

        if (currentAnimation == null) {
            // If the current animation is null, we should try to play the next animation.
            tryPlayNextOrStop(context);
            return;
        }

        currentAnimation.playBehavior().onUpdate(context);

        // At this point we have an animation currently playing. We need to query if that animation has finished.

        var animContext = context.animationContext();
        var animatable = animContext.animatable();
        var hasAnimationFinished = controllerTimer.getAdjustedTick() >= currentAnimation.animation().length();

        if (hasAnimationFinished) {
            currentAnimation.playBehavior().onFinish(context);
        }

        if (context.stateMachine().isStopped()) {
            // Nothing more to do at this point since we can't play the animation again, so return.
            return;
        }

        // The animation is still running at this point, proceed with updating the bones according to keyframes.

        var keyframeManager = controller.keyframeManager();
        var keyframeExecutor = keyframeManager.keyframeExecutor();
        var crashWhenCantFindBone = animContext.config().crashIfBoneMissing();

        keyframeExecutor.execute(currentAnimation, animatable, crashWhenCantFindBone);
    }

    private void tryPlayNextOrStop(AzAnimationControllerStateMachine.Context<T> context) {
        var controller = context.animationController();
        var stateMachine = context.stateMachine();
        var keyframeManager = controller.keyframeManager();
        var keyframeCallbackHandler = keyframeManager.keyframeCallbackHandler();

        keyframeCallbackHandler.reset();

        var animationQueue = controller.animationQueue();
        var nextAnimation = animationQueue.peek();

        if (nextAnimation == null) {
            // If we can't play the next animation for some reason, then there's nothing to play.
            // So we should put the state machine in the 'stop' state.
            stateMachine.stop();
            return;
        }

        // If we can play the next animation successfully, then let's do that.
        stateMachine.transition();
        controller.setCurrentAnimation(nextAnimation);
    }
}
