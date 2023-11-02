package mod.azure.azurelib.common.internal.common.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.common.internal.common.core.keyframe.Keyframe;

/**
 * Sound {@link Keyframe} instruction holder
 */
public class SoundKeyframeData extends KeyFrameData {
	private final String sound;

	public SoundKeyframeData(Double startTick, String sound) {
		super(startTick);

		this.sound = sound;
	}

	/**
	 * Gets the sound id given by the {@link Keyframe} instruction from the {@code animation.json}
	 */
	public String getSound() {
		return this.sound;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getStartTick(), this.sound);
	}
}
