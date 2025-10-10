package mod.azure.azurelib.common.animation.play_behavior;

import mod.azure.azurelib.common.animation.controller.state.machine.AzAnimationControllerStateMachine;

public abstract class AzPlayBehavior {

    private final String name;

    protected AzPlayBehavior(String name) {
        this.name = name;
    }

    public void onUpdate(AzAnimationControllerStateMachine.Context<?> context) {}

    public void onFinish(AzAnimationControllerStateMachine.Context<?> context) {}

    public String name() {
        return name;
    }
}
