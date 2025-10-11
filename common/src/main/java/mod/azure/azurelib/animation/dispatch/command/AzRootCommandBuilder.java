package mod.azure.azurelib.animation.dispatch.command;

import java.util.function.UnaryOperator;

import mod.azure.azurelib.animation.dispatch.command.action.impl.root.*;
import mod.azure.azurelib.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.animation.easing.AzEasingType;

/**
 * AzRootCommandBuilder is a concrete implementation of AzCommandBuilder that provides methods specifically tailored for
 * constructing and configuring root-level animation commands. These methods allow for appending subcommands, adjusting
 * easing types, speeds, and playback sequences, as well as performing cancel operations or configuring other
 * root-command-specific actions.
 */
public class AzRootCommandBuilder extends AzCommandBuilder {

    /**
     * Appends the actions of the specified command to the current root command builder.
     *
     * @param command the command whose actions are to be appended
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder append(AzCommand command) {
        actions.addAll(command.actions());
        return this;
    }

    /**
     * Cancels all ongoing animations for all animation controllers associated with the current animator. This method
     * adds an action to the command builder that, when executed, clears the current animation from all animation
     * controllers in the animator.
     *
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder cancelAll() {
        actions.add(AzRootCancelAllAction.INSTANCE);
        return this;
    }

    /**
     * Sets the easing type to be used for root-level animations within the command builder. The easing type defines the
     * interpolation behavior for transitioning animations.
     *
     * @param easingType the {@code AzEasingType} instance that specifies the desired easing behavior
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setEasingType(AzEasingType easingType) {
        actions.add(new AzRootSetEasingTypeAction(easingType));
        return this;
    }

    /**
     * Sets the animation speed for all root-level animations within the command builder. This method adds an action to
     * adjust the speed of animations when executed.
     *
     * @param speed the desired animation speed, where a value of 1.0 represents the normal animation speed
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setSpeed(float speed) {
        actions.add(new AzRootSetAnimationSpeedAction(speed));
        return this;
    }

    /**
     * Sets the transition speed for animations and adds the corresponding action to the command builder. The transition
     * speed determines the duration of the transition between animation states.
     *
     * @param transitionSpeed a float representing the transition speed to be applied
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setTransitionSpeed(float transitionSpeed) {
        actions.add(new AzRootSetTransitionSpeedAction(transitionSpeed));
        return this;
    }

    /**
     * Sets the start tick offset for root-level animations within the command builder. This method adds an action that,
     * when executed, adjusts the animation's start point based on the specified tick offset.
     *
     * @param tickOffset the float value representing the tick offset to shift the start of the animation
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setStartTickOffset(float tickOffset) {
        actions.add(new AzRootSetStartTickOffsetAction(tickOffset));
        return this;
    }

    /**
     * Sets the freeze tick offset for root-level animations within the command builder. This method adds an action
     * that, when executed, adjusts the freeze tick offset of animations based on the specified value.
     *
     * @param freezeTickOffset the float value representing the freeze tick offset to be applied to the animation
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setFreezeTickOffset(float freezeTickOffset) {
        actions.add(new AzRootSetFreezeTickAction(freezeTickOffset));
        return this;
    }

    /**
     * Sets the repeat amount for root-level animations within the command builder. This method adds an action that,
     * when executed, adjusts the number of times the animation should repeat based on the specified value.
     *
     * @param repeatAmount the float value representing the number of times the animation should repeat
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setRepeatAmount(float repeatAmount) {
        actions.add(new AzRootSetRepeatTimesAction(repeatAmount));
        return this;
    }

    /**
     * Sets whether the animations should play in reverse order and adds the corresponding action to the command
     * builder. When executed, this action updates the animation properties of all relevant animation controllers to
     * reflect the reverse playback setting.
     *
     * @param hasReverse a boolean value indicating whether animations should play in reverse
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setReverseAnimation(boolean hasReverse) {
        actions.add(new AzRootSetReverseAction(hasReverse));
        return this;
    }

    /**
     * Plays a composed animation sequence by applying the provided {@link UnaryOperator} to an instance of
     * {@code AzAnimationSequenceBuilder}. The animation sequence is constructed and then added as an action to the
     * current root command builder.
     *
     * @param builderUnaryOperator a {@link UnaryOperator} to customize and build an {@code AzAnimationSequence} using
     *                             an {@link AzAnimationSequenceBuilder}
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder playSequence(
        UnaryOperator<AzAnimationSequenceBuilder> builderUnaryOperator
    ) {
        var sequence = builderUnaryOperator.apply(new AzAnimationSequenceBuilder()).build();
        actions.add(new AzRootPlayAnimationSequenceAction(sequence));
        return this;
    }
}
