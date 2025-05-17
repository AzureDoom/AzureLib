package mod.azure.azurelib.animation.controller.state.impl;

import mod.azure.azurelib.animation.AzAnimationContext;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzAnimationControllerTimer;
import mod.azure.azurelib.animation.controller.AzAnimationQueue;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeCallbackHandler;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeExecutor;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeManager;
import mod.azure.azurelib.animation.controller.state.AzAnimationState;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;

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
        AzAnimationController<T> controller = context.animationController();
        AzAnimationControllerTimer<T> controllerTimer = controller.controllerTimer();

        controllerTimer.reset();
    }

    @Override
    public void onUpdate(AzAnimationControllerStateMachine.Context<T> context) {
        AzAnimationController<T> controller = context.animationController();
        AzAnimationControllerTimer<T> controllerTimer = controller.controllerTimer();
        AzQueuedAnimation currentAnimation = controller.currentAnimation();

        if (currentAnimation == null) {
            // If the current animation is null, we should try to play the next animation.
            tryPlayNextOrStop(context);
            return;
        }

        currentAnimation.playBehavior().onUpdate(context);

        // At this point we have an animation currently playing. We need to query if that animation has finished.

        AzAnimationContext<T> animContext = context.animationContext();
        T animatable = animContext.animatable();
        boolean hasAnimationFinished = controllerTimer.getAdjustedTick() >= currentAnimation.animation().length();

        if (hasAnimationFinished) {
            currentAnimation.playBehavior().onFinish(context);
        }

        if (context.stateMachine().isStopped()) {
            // Nothing more to do at this point since we can't play the animation again, so return.
            return;
        }

        // The animation is still running at this point, proceed with updating the bones according to keyframes.

        AzKeyframeManager<T> keyframeManager = controller.keyframeManager();
        AzKeyframeExecutor<T> keyframeExecutor = keyframeManager.keyframeExecutor();
        boolean crashWhenCantFindBone = animContext.config().crashIfBoneMissing();

        keyframeExecutor.execute(currentAnimation, animatable, crashWhenCantFindBone);
    }

    private void tryPlayNextOrStop(AzAnimationControllerStateMachine.Context<T> context) {
        AzAnimationController<T> controller = context.animationController();
        AzAnimationControllerStateMachine<T> stateMachine = context.stateMachine();
        AzKeyframeManager<T> keyframeManager = controller.keyframeManager();
        AzKeyframeCallbackHandler<T> keyframeCallbackHandler = keyframeManager.keyframeCallbackHandler();

        keyframeCallbackHandler.reset();

        AzAnimationQueue animationQueue = controller.animationQueue();
        AzQueuedAnimation nextAnimation = animationQueue.peek();

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
