package mod.azure.azurelib.rewrite.animation.dispatch.command;

import java.util.function.UnaryOperator;

import mod.azure.azurelib.rewrite.animation.dispatch.command.action.impl.root.*;
import mod.azure.azurelib.rewrite.animation.dispatch.command.sequence.AzAnimationSequenceBuilder;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;

/**
 * AzRootCommandBuilder is a concrete implementation of AzCommandBuilder that provides methods
 * specifically tailored for constructing and configuring root-level animation commands. These methods
 * allow for appending subcommands, adjusting easing types, speeds, and playback sequences, as well as performing
 * cancel operations or configuring other root command-specific actions.
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
     * Cancels all ongoing animations for all animation controllers associated with the current animator.
     * This method adds an action to the command builder that, when executed, clears the current animation
     * from all animation controllers in the animator.
     *
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder cancelAll() {
        actions.add(AzRootCancelAllAction.INSTANCE);
        return this;
    }

    /**
     * Sets the easing type to be used for root-level animations within the command builder.
     * The easing type defines the interpolation behavior for transitioning animations.
     *
     * @param easingType the {@code AzEasingType} instance that specifies the desired easing behavior
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setEasingType(AzEasingType easingType) {
        actions.add(new AzRootSetEasingTypeAction(easingType));
        return this;
    }

    /**
     * Sets the animation speed for all root-level animations within the command builder.
     * This method adds an action to adjust the speed of animations when executed.
     *
     * @param speed the desired animation speed, where a value of 1.0 represents the normal animation speed
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setSpeed(float speed) {
        actions.add(new AzRootSetAnimationSpeedAction(speed));
        return this;
    }

    /**
     * Sets the transition speed for animations and adds the corresponding action to the command builder.
     * The transition speed determines the duration of the transition between animation states.
     *
     * @param transitionSpeed a float representing the transition speed to be applied
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setTransitionSpeed(float transitionSpeed) {
        actions.add(new AzRootSetTransitionSpeedAction(transitionSpeed));
        return this;
    }

    /**
     * Sets the start tick offset for root-level animations within the command builder.
     * This method adds an action that, when executed, adjusts the animation's start
     * point based on the specified tick offset.
     *
     * @param tickOffset the float value representing the tick offset to shift the start of the animation
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder setStartTickOffset(float tickOffset) {
        actions.add(new AzRootSetStartTickOffsetAction(tickOffset));
        return this;
    }

    /**
     * Cancels the current animation for a specified animation controller by adding a cancel action
     * to the command builder. This action targets the animation controller identified by its name.
     *
     * @param controllerName the name of the animation controller whose animation will be canceled
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder cancel(String controllerName) {
        actions.add(new AzRootCancelAction(controllerName));
        return this;
    }

    /**
     * Plays the specified animation on the given controller. This method queues the provided
     * animation name into an animation sequence and associates it with the specified
     * controller.
     *
     * @param controllerName the name of the animation controller on which to play the animation
     * @param animationName the name of the animation to be played
     * @return the updated instance of {@code AzRootCommandBuilder} for method chaining
     */
    public AzRootCommandBuilder play(String controllerName, String animationName) {
        return playSequence(controllerName, builder -> builder.queue(animationName, properties -> properties));
    }

    /**
     * Adds an action to play a sequence of animations on the specified controller.
     * The animation sequence is created using the provided builder and then executed
     * when the command is run.
     *
     * @param controllerName the name of the animation controller on which the sequence will be played
     * @param builderUnaryOperator a {@code UnaryOperator} that defines and customizes the animation sequence
     *                              using an instance of {@code AzAnimationSequenceBuilder}
     * @return the current instance of {@code AzRootCommandBuilder} to support method chaining
     */
    public AzRootCommandBuilder playSequence(
        String controllerName,
        UnaryOperator<AzAnimationSequenceBuilder> builderUnaryOperator
    ) {
        var sequence = builderUnaryOperator.apply(new AzAnimationSequenceBuilder()).build();
        actions.add(new AzRootPlayAnimationSequenceAction(controllerName, sequence));
        return this;
    }
}
