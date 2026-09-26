package mod.azure.azurelib.animation.dispatch.command;

import java.util.function.UnaryOperator;

import mod.azure.azurelib.animation.dispatch.command.action.impl.controller.*;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzSequence;
import mod.azure.azurelib.animation.easing.AzEasingType;

public class AzControllerCommandBuilder extends AzCommandBuilder {

    public AzControllerCommandBuilder append(AzCommand command) {
        actions.addAll(command.actions());
        return this;
    }

    public AzControllerCommandBuilder setEasingType(String controllerName, AzEasingType easingType) {
        actions.add(new AzControllerSetEasingTypeAction(controllerName, easingType));
        return this;
    }

    public AzControllerCommandBuilder setSpeed(String controllerName, float speed) {
        actions.add(new AzControllerSetAnimationSpeedAction(controllerName, speed));
        return this;
    }

    public AzControllerCommandBuilder setTransitionSpeed(String controllerName, float transitionSpeed) {
        actions.add(new AzControllerSetTransitionSpeedAction(controllerName, transitionSpeed));
        return this;
    }

    public AzControllerCommandBuilder setStartTickOffset(String controllerName, float tickOffset) {
        actions.add(new AzControllerSetStartTickOffsetAction(controllerName, tickOffset));
        return this;
    }

    public AzControllerCommandBuilder setFreezeTickOffset(String controllerName, float freezeTickOffset) {
        actions.add(new AzControllerSetFreezeTickAction(controllerName, freezeTickOffset));
        return this;
    }

    public AzControllerCommandBuilder setRepeatAmount(String controllerName, float repeatAmount) {
        actions.add(new AzControllerSetRepeatTimesAction(controllerName, repeatAmount));
        return this;
    }

    public AzControllerCommandBuilder setReverseAnimation(String controllerName, boolean hasReverse) {
        actions.add(new AzControllerSetReverseAction(controllerName, hasReverse));
        return this;
    }

    /**
     * Sets a controller's blend weight (0 to 1) immediately. See {@code AzAnimationController#setWeight}.
     */
    public AzControllerCommandBuilder setWeight(String controllerName, float weight) {
        actions.add(new AzControllerSetWeightAction(controllerName, weight, 0));
        return this;
    }

    /**
     * Fades a controller's blend weight (0 to 1) over {@code fadeTicks} ticks. See
     * {@code AzAnimationController#fadeWeight}.
     */
    public AzControllerCommandBuilder fadeWeight(String controllerName, float weight, float fadeTicks) {
        actions.add(new AzControllerSetWeightAction(controllerName, weight, fadeTicks));
        return this;
    }

    public AzControllerCommandBuilder cancel(String controllerName) {
        actions.add(new AzControllerCancelAction(controllerName));
        return this;
    }

    public AzControllerCommandBuilder play(String controllerName, String animationName) {
        return playSequence(controllerName, builder -> builder.queue(animationName, properties -> properties));
    }

    public AzControllerCommandBuilder playSequence(
        String controllerName,
        UnaryOperator<AzAnimationSequenceBuilder> builderUnaryOperator
    ) {
        var sequence = builderUnaryOperator.apply(new AzAnimationSequenceBuilder()).build();
        actions.add(new AzControllerPlayAnimationSequenceAction(controllerName, sequence));
        return this;
    }

    /**
     * Plays an {@link AzSequence} on the given controller. Sequence events are not dispatched; use an
     * {@link mod.azure.azurelib.animation.dispatch.command.sequence.AzSequencePlayer} for those.
     */
    public AzControllerCommandBuilder playSequence(String controllerName, AzSequence sequence) {
        actions.add(new AzControllerPlayAnimationSequenceAction(controllerName, sequence.toAnimationSequence()));
        return this;
    }
}
