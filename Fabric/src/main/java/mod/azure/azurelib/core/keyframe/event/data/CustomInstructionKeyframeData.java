package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.animation.controller.keyframe.AzKeyframe;

/**
 * Custom instruction {@link AzKeyframe} instruction holder
 */
public class CustomInstructionKeyframeData extends KeyFrameData {

    private final String instructions;

    public CustomInstructionKeyframeData(double startTick, String instructions) {
        super(startTick);

        this.instructions = instructions;
    }

    /**
     * Gets the instructions string given by the {@link AzKeyframe} instruction from the {@code animation.json}
     */
    public String getInstructions() {
        return this.instructions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStartTick(), instructions);
    }
}
