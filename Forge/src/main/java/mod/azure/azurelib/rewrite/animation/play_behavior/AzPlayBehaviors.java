package mod.azure.azurelib.rewrite.animation.play_behavior;

import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationControllerTimer;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzKeyframeCallbackHandler;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzKeyframeManager;
import mod.azure.azurelib.rewrite.animation.controller.state.machine.AzAnimationControllerStateMachine;

public class AzPlayBehaviors {

    public static final AzPlayBehavior HOLD_ON_LAST_FRAME = AzPlayBehaviorRegistry.register(
        new AzPlayBehavior("hold_on_last_frame") {

            @Override
            public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
                context.stateMachine().pause();
            }
        }
    );

    public static final AzPlayBehavior LOOP = AzPlayBehaviorRegistry.register(new AzPlayBehavior("loop") {

        @Override
        public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
            AzAnimationController<?> controller = context.animationController();
            AzAnimationControllerTimer<?> controllerTimer = controller.controllerTimer();
            AzKeyframeManager<?> keyframeManager = controller.keyframeManager();
            AzKeyframeCallbackHandler<?> keyframeCallbackHandler = keyframeManager.keyframeCallbackHandler();

            controllerTimer.reset();
            keyframeCallbackHandler.reset();
        }
    });

    public static final AzPlayBehavior PLAY_ONCE = AzPlayBehaviorRegistry.register(new AzPlayBehavior("play_once") {

        @Override
        public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {
            context.stateMachine().stop();
        }
    });
}
