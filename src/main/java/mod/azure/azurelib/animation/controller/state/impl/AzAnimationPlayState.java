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

    /**
     * Handles the behavior when the play state is entered in the animation state machine. This method resets the
     * animation controller's timer to synchronize the starting point of the animation, ensuring consistency with the
     * current animation state.
     *
     * @param context the context of the animation state machine, providing access to the animation controller,
     *                animation context, and state machine properties
     */
    @Override
    public void onEnter(AzAnimationControllerStateMachine.Context<T> context) {
        super.onEnter(context);
        AzAnimationController<T> controller = context.animationController();
        AzAnimationControllerTimer<T> controllerTimer = controller.controllerTimer();

        controllerTimer.reset();
    }

    /**
     * Updates the state of the animation controller as part of the current animation play state. This method handles
     * playing the animation, checking for animation completion, and applying keyframe transformations to the animatable
     * object. If no current animation is playing, it attempts to transition to the next animation or stops the state
     * machine if no animations are queued.
     *
     * @param context the context of the animation state machine, providing access to the animation controller,
     *                animation context, and state machine properties
     */
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

        AzAnimationControllerStateMachine<T> stateMachine = context.stateMachine();

        if (stateMachine.isStopped() || stateMachine.isTransitioning()) {
            // Either nothing more will play, or the play behavior moved on to the next queued stage. In the latter
            // case the controller now holds the next animation and its transition has already snapshot the bones,
            // so executing the finished animation's keyframes here would flash its first frame for one update.
            return;
        }

        // The animation is still running at this point, proceed with updating the bones according to keyframes.

        AzKeyframeManager<T> keyframeManager = controller.keyframeManager();
        AzKeyframeExecutor<T> keyframeExecutor = keyframeManager.keyframeExecutor();
        boolean crashWhenCantFindBone = animContext.config().crashIfBoneMissing();

        keyframeExecutor.execute(currentAnimation, animatable, crashWhenCantFindBone);
    }

    /**
     * Attempts to play the next animation in the queue or transitions the state machine to the "stop" state if no
     * animations are available. This method checks the animation queue for a pending animation. If an animation is
     * available, it is set as the current animation, and the state machine transitions to a play state. If no animation
     * is available, the state machine transitions to the "stop" state, effectively halting any further actions in the
     * animation system.
     *
     * @param context the context of the animation state machine, containing the animation controller, state machine
     *                instance, and associated data used to manage animation state and transitions
     */
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
