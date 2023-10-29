/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib.core.keyframe.event.data;

import java.util.Objects;

import mod.azure.azurelib.core.keyframe.Keyframe;

/**
 * Base class for custom {@link Keyframe} events.<br>
 * @see ParticleKeyframeData
 * @see SoundKeyframeData
 */
public abstract class KeyFrameData {
	private final double startTick;

	protected KeyFrameData(double startTick) {
		this.startTick = startTick;
	}

	/**
	 * Gets the start tick of the keyframe instruction
	 */
	public double getStartTick() {
		return this.startTick;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		return this.hashCode() == obj.hashCode();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.startTick);
	}
}
