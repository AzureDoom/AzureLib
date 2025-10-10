package mod.azure.azurelib.common.animation.dispatch.command.sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import mod.azure.azurelib.common.animation.dispatch.command.stage.AzAnimationStage;
import mod.azure.azurelib.common.animation.property.AzAnimationStageProperties;

/**
 * Builder class for creating instances of {@link AzAnimationSequence}.
 * <p>
 * The {@code AzAnimationSequenceBuilder} provides a fluent API for defining and queuing animation stages to be included
 * in a sequence. Each stage is defined by its name and can optionally include customized properties through a
 * functional interface.
 */
public class AzAnimationSequenceBuilder {

    private final List<AzAnimationStage> stages;

    public AzAnimationSequenceBuilder() {
        this.stages = new ArrayList<>();
    }

    /**
     * Adds a new animation stage to the sequence with the specified animation name. The stage is created using the
     * given animation name and default (empty) stage properties.
     *
     * @param animationName the name of the animation to be added as a stage
     * @return the current instance of {@code AzAnimationSequenceBuilder} to allow method chaining
     */
    public AzAnimationSequenceBuilder queue(String animationName) {
        stages.add(new AzAnimationStage(animationName, AzAnimationStageProperties.EMPTY));
        return this;
    }

    /**
     * Adds a new animation stage to the sequence with the specified animation name and optionally customized
     * properties. The stage is created using the given animation name and the properties provided by the
     * {@code builderUnaryOperator}.
     *
     * @param animationName        the name of the animation to be added as a stage
     * @param builderUnaryOperator a unary operator to customize the {@code AzAnimationStageProperties} of the animation
     *                             stage
     * @return the current instance of {@code AzAnimationSequenceBuilder} to allow method chaining
     */
    public AzAnimationSequenceBuilder queue(
        String animationName,
        UnaryOperator<AzAnimationStageProperties> builderUnaryOperator
    ) {
        var properties = builderUnaryOperator.apply(AzAnimationStageProperties.EMPTY);
        stages.add(new AzAnimationStage(animationName, properties));
        return this;
    }

    /**
     * Builds and returns a new {@code AzAnimationSequence} instance constructed from the queued animation stages.
     *
     * @return a new {@code AzAnimationSequence} instance containing the defined animation stages
     */
    public AzAnimationSequence build() {
        return new AzAnimationSequence(stages);
    }
}
