/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.common.animation.controller.keyframe.AzKeyframe;

/**
 * Custom instruction {@link AzKeyframe} instruction holder
 */
@SuppressWarnings("unused")
public class CustomInstructionKeyframeData extends KeyFrameData {

    private final String instructions;

    private final int hashCode;

    public CustomInstructionKeyframeData(double startTick, String instructions) {
        super(startTick);

        this.instructions = instructions;
        this.hashCode = 31 * (31 + Double.hashCode(startTick)) + Objects.hashCode(instructions);
    }

    /**
     * Gets the instructions string given by the {@link AzKeyframe} instruction from the {@code animation.json}
     */
    public String getInstructions() {
        return this.instructions;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
