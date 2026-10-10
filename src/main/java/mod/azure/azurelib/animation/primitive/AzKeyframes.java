package mod.azure.azurelib.animation.primitive;

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
public final class AzKeyframes {

    private final SoundKeyframeData[] sounds;

    private final ParticleKeyframeData[] particles;

    private final CustomInstructionKeyframeData[] customInstructions;

    public AzKeyframes(
        SoundKeyframeData[] sounds,
        ParticleKeyframeData[] particles,
        CustomInstructionKeyframeData[] customInstructions
    ) {
        this.sounds = sounds;
        this.particles = particles;
        this.customInstructions = customInstructions;
    }

    public SoundKeyframeData[] sounds() {
        return this.sounds;
    }

    public ParticleKeyframeData[] particles() {
        return this.particles;
    }

    public CustomInstructionKeyframeData[] customInstructions() {
        return this.customInstructions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzKeyframes))
            return false;
        AzKeyframes other = (AzKeyframes) o;
        return java.util.Objects.equals(this.sounds, other.sounds)
            && java.util.Objects.equals(this.particles, other.particles)
            && java.util.Objects.equals(this.customInstructions, other.customInstructions);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.sounds);
        result = 31 * result + java.util.Objects.hashCode(this.particles);
        result = 31 * result + java.util.Objects.hashCode(this.customInstructions);
        return result;
    }

    @Override
    public String toString() {
        return "AzKeyframes[sounds=" + this.sounds + ", particles=" + this.particles + ", customInstructions="
            + this.customInstructions + "]";
    }
}
