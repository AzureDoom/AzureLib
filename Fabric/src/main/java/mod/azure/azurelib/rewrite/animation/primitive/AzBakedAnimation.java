package mod.azure.azurelib.rewrite.animation.primitive;

import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzBoneAnimation;

/**
 * A compiled animation instance for use by the {@link AzAnimationController}<br>
 * Modifications or extensions of a compiled Animation are not supported, and therefore an instance of
 * <code>Animation</code> is considered final and immutable.
 */
public class AzBakedAnimation {
	private final String name;
	private final double length;
	private final AzLoopType loopType;
	private final AzBoneAnimation[] boneAnimations;
	private final AzKeyframes keyframes;

	public AzBakedAnimation(String name, double length, AzLoopType loopType, AzBoneAnimation[] boneAnimations, AzKeyframes keyframes) {
		this.name = name;
		this.length = length;
		this.loopType = loopType;
		this.boneAnimations = boneAnimations;
		this.keyframes = keyframes;
	}

	public String name() {
		return name;
	}

	public double length() {
		return length;
	}

	public AzLoopType loopType() {
		return loopType;
	}

	public AzBoneAnimation[] boneAnimations() {
		return boneAnimations;
	}

	public AzKeyframes keyframes() {
		return keyframes;
	}

	@Override
	public String toString() {
		return "AzBakedAnimation{" +
			       "name='" + name + '\'' +
			       ", length=" + length +
			       ", loopType=" + loopType +
			       ", boneAnimations=" + java.util.Arrays.toString(boneAnimations) +
			       ", keyframes=" + keyframes +
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

		AzBakedAnimation that = (AzBakedAnimation) o;

		if (Double.compare(that.length, length) != 0) {
			return false;
		}
		if (!name.equals(that.name)) {
			return false;
		}
		if (!loopType.equals(that.loopType)) {
			return false;
		}
		if (!java.util.Arrays.equals(boneAnimations, that.boneAnimations)) {
			return false;
		}
		return keyframes.equals(that.keyframes);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = name.hashCode();
		temp = Double.doubleToLongBits(length);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + loopType.hashCode();
		result = 31 * result + java.util.Arrays.hashCode(boneAnimations);
		result = 31 * result + keyframes.hashCode();
		return result;
	}
}

