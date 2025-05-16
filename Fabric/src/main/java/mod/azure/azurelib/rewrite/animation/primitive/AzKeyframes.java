package mod.azure.azurelib.rewrite.animation.primitive;

import mod.azure.azurelib.core.keyframe.event.data.CustomInstructionKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.SoundKeyframeData;

/**
 * Represents a collection of keyframe data used for animations. <br/>
 * The AzKeyframes record combines different types of keyframe data into a single structure:
 * <ul>
 * <li>{@link SoundKeyframeData} for sound-related keyframes.</li>
 * <li>{@link ParticleKeyframeData} for particle effect-related keyframes.</li>
 * <li>{@link CustomInstructionKeyframeData} for custom instruction keyframes.</li>
 * </ul>
 * <br/>
 * This record organizes and provides access to all three types of keyframe data, enabling cohesive handling of
 * animation sequences defined in an animation system.
 */
public class AzKeyframes {
	private final SoundKeyframeData[] sounds;
	private final ParticleKeyframeData[] particles;
	private final CustomInstructionKeyframeData[] customInstructions;

	public AzKeyframes(SoundKeyframeData[] sounds,
	                   ParticleKeyframeData[] particles,
	                   CustomInstructionKeyframeData[] customInstructions) {
		this.sounds = sounds;
		this.particles = particles;
		this.customInstructions = customInstructions;
	}

	public SoundKeyframeData[] sounds() {
		return sounds;
	}

	public ParticleKeyframeData[] particles() {
		return particles;
	}

	public CustomInstructionKeyframeData[] customInstructions() {
		return customInstructions;
	}

	@Override
	public String toString() {
		return "AzKeyframes{" +
			       "sounds=" + java.util.Arrays.toString(sounds) +
			       ", particles=" + java.util.Arrays.toString(particles) +
			       ", customInstructions=" + java.util.Arrays.toString(customInstructions) +
			       '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		AzKeyframes that = (AzKeyframes) o;

		if (!java.util.Arrays.equals(sounds, that.sounds)) {
			return false;
		}
		if (!java.util.Arrays.equals(particles, that.particles)) {
			return false;
		}
		return java.util.Arrays.equals(customInstructions, that.customInstructions);
	}

	@Override
	public int hashCode() {
		int result = java.util.Arrays.hashCode(sounds);
		result = 31 * result + java.util.Arrays.hashCode(particles);
		result = 31 * result + java.util.Arrays.hashCode(customInstructions);
		return result;
	}
}
