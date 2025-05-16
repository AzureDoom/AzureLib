package mod.azure.azurelib.rewrite.animation.controller.keyframe;

import mod.azure.azurelib.core.keyframe.Keyframe;
import mod.azure.azurelib.core.math.IValue;

public class AzBoneAnimation {
	private final String boneName;
	private final AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes;
	private final AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes;
	private final AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes;

	/**
	 * A record of a deserialized animation for a given bone.<br>
	 * Responsible for holding the various {@link Keyframe Keyframes} for the bone's animation transformations
	 *
	 * @param boneName          The name of the bone as listed in the {@code animation.json}
	 * @param rotationKeyframes The deserialized rotation {@code Keyframe} stack
	 * @param positionKeyframes The deserialized position {@code Keyframe} stack
	 * @param scaleKeyframes    The deserialized scale {@code Keyframe} stack
	 */
	public AzBoneAnimation(String boneName,
	                       AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes,
	                       AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes,
	                       AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes) {
		this.boneName = boneName;
		this.rotationKeyframes = rotationKeyframes;
		this.positionKeyframes = positionKeyframes;
		this.scaleKeyframes = scaleKeyframes;
	}

	public String boneName() {
		return boneName;
	}

	public AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes() {
		return rotationKeyframes;
	}

	public AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes() {
		return positionKeyframes;
	}

	public AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes() {
		return scaleKeyframes;
	}

	@Override
	public String toString() {
		return "AzBoneAnimation{" +
			       "boneName='" + boneName + '\'' +
			       ", rotationKeyframes=" + rotationKeyframes +
			       ", positionKeyframes=" + positionKeyframes +
			       ", scaleKeyframes=" + scaleKeyframes +
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

		AzBoneAnimation that = (AzBoneAnimation) o;

		if (!boneName.equals(that.boneName)) {
			return false;
		}
		if (!rotationKeyframes.equals(that.rotationKeyframes)) {
			return false;
		}
		if (!positionKeyframes.equals(that.positionKeyframes)) {
			return false;
		}
		return scaleKeyframes.equals(that.scaleKeyframes);
	}

	@Override
	public int hashCode() {
		int result = boneName.hashCode();
		result = 31 * result + rotationKeyframes.hashCode();
		result = 31 * result + positionKeyframes.hashCode();
		result = 31 * result + scaleKeyframes.hashCode();
		return result;
	}
}

