package mod.azure.azurelib.animation.play_behavior;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.state.machine.AzAnimationControllerStateMachine;

public class AzPlayBehaviors {

    private AzPlayBehaviors() {}

    /**
     * Placeholder meaning "use whatever the animation file says" (its {@code loop}, {@code repeat_times} and
     * {@code freeze_at} fields, as written by the AzureLib Blockbench plugin). It is swapped for the real behavior when
     * the animation is queued, so it never actually runs; stages that don't set a behavior at all are treated the same
     * way. Files without a {@code loop} field fall back to {@link #PLAY_ONCE}.
     */
    public static final AzPlayBehavior AS_AUTHORED = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("as_authored") {

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                // Unreachable in practice (resolved at queue time); behave like play_once if it ever leaks through.
                advanceOrStop(context);
            }
        }
    );

    /**
     * Plays the animation forward, then backward, then forward again, indefinitely. Each leg starts on the pose the
     * previous one ended on, so the turnaround is seamless without the animation having to be authored as a loop.
     * <p>
     * The leg direction is stored on each {@link AzAnimationController} ({@code flipDirection()}), not on this shared
     * instance. If the stage itself is reversed, the first leg runs backward. Like {@link #LOOP}, it never finishes, so
     * nothing can be queued after it. Keyframe events fire once per leg as the sampled tick passes them, so an event at
     * tick 10 fires on the way out and again on the way back; an event exactly at either end fires once per turnaround.
     */
    public static final AzPlayBehavior PING_PONG = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("ping_pong") {

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                var controller = context.animationController();

                controller.flipDirection();
                controller.controllerTimer().reset();
                controller.keyframeManager().keyframeCallbackHandler().reset();
            }
        }
    );

    /**
     * Represents a play behavior where an animation is repeated a specified number of times. The behavior resets the
     * animation controller's timer and keyframe callback handler after each iteration and continues playing until the
     * maximum repeat count is reached. Once the repeat count is met, the controller moves on to the next queued stage,
     * or stops if there is none.
     * <p>
     * The repeat count is stored on each {@link AzAnimationController}, not on this shared behavior instance, so
     * controllers repeating at the same time do not interfere with each other.
     */
    public static final AzPlayBehavior REPEAT_X_TIMES = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("repeat_x_times") {

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                AzAnimationController<?> controller = context.animationController();
                var maxRepeats = controller.animationProperties().repeatXTimes();
                var repeatCount = controller.incrementRepeatCount();

                if (maxRepeats > 1 && repeatCount <= maxRepeats) {
                    var controllerTimer = controller.controllerTimer();
                    var keyframeManager = controller.keyframeManager();
                    var keyframeCallbackHandler = keyframeManager.keyframeCallbackHandler();

                    controllerTimer.reset();
                    keyframeCallbackHandler.reset();

                    context.stateMachine().play();
                } else {
                    controller.resetRepeatCount();
                    advanceOrStop(context);
                }
            }
        }
    );

    /**
     * A predefined {@code AzPlayBehavior} that freezes the animation at a specific frame and pauses the state machine.
     */
    public static final AzPlayBehavior FREEZE_ON_FRAME = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("freeze_on_frame") {

            @Override
            public void onUpdate(AzAnimationControllerStateMachine.Context<?> context) {
                var controller = context.animationController();
                var controllerTimer = controller.controllerTimer();
                var freezeTickOffset = controller.animationProperties().freezeTickOffset();

                if (controllerTimer.getAdjustedTick() >= freezeTickOffset) {
                    controllerTimer.addToAdjustedTick(0);
                    context.stateMachine().pause();
                }
            }

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                context.stateMachine().pause();
            }
        }
    );

    /**
     * Represents a play behavior where an animation holds on its last frame upon completion. When the animation
     * finishes, the associated state machine is paused, effectively freezing the animation on the final frame.
     */
    public static final AzPlayBehavior HOLD_ON_LAST_FRAME = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("hold_on_last_frame") {

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                context.stateMachine().pause();
            }
        }
    );

    /**
     * A predefined {@link AzPlayBehavior} that loops an animation indefinitely.
     */
    public static final AzPlayBehavior LOOP = AzPlayBehaviorRegistry.register(new AzPlayBehavior("loop") {

        @Override
        public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
            var controller = context.animationController();
            var controllerTimer = controller.controllerTimer();
            var keyframeManager = controller.keyframeManager();
            var keyframeCallbackHandler = keyframeManager.keyframeCallbackHandler();

            controllerTimer.reset();
            keyframeCallbackHandler.reset();
        }
    });

    /**
     * A predefined {@link AzPlayBehavior} that plays an animation once. When it finishes, the controller moves on to
     * the next queued stage of the sequence, or stops if this was the last one.
     */
    public static final AzPlayBehavior PLAY_ONCE = AzPlayBehaviorRegistry.register(new AzPlayBehavior("play_once") {

        @Override
        public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
            advanceOrStop(context);
        }
    });

    /**
     * Transitions to the next queued animation of the current sequence, or stops the state machine if the queue is
     * empty. Behaviors that "end" an animation should call this rather than {@code stop()}, otherwise the remaining
     * stages of a multi-stage sequence never play.
     */
    public static void advanceOrStop(AzAnimationControllerStateMachine.Context<?> context) {
        if (context.animationController().animationQueue().isEmpty()) {
            context.stateMachine().stop();
        } else {
            context.stateMachine().transition();
        }
    }
}
