package mod.azure.azurelib.core.animation;

import mod.azure.azurelib.core.keyframe.event.data.CustomInstructionKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.SoundKeyframeData;

public class Keyframes {

	public SoundKeyframeData[] sounds;
	public ParticleKeyframeData[] particles;
	public CustomInstructionKeyframeData[] customInstructions;

	public Keyframes(SoundKeyframeData[] sounds, ParticleKeyframeData[] particles, CustomInstructionKeyframeData[] customInstructions) {
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
}
