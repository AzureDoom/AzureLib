/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.core.keyframe.Keyframe;

/**
 * Custom instruction {@link Keyframe} instruction holder
 */
public class CustomInstructionKeyframeData extends KeyFrameData {
	private final String instructions;

	public CustomInstructionKeyframeData(double startTick, String instructions) {
		super(startTick);

		this.instructions = instructions;
	}

	/**
	 * Gets the instructions string given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getInstructions() {
		return this.instructions;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartTick(), instructions);
	}
}
