package mod.azure.azurelib.rewrite.animation.primitive;

import mod.azure.azurelib.rewrite.animation.play_behavior.AzPlayBehavior;

public class AzQueuedAnimation {
	private final AzBakedAnimation animation;
	private final AzPlayBehavior playBehavior;

	public AzQueuedAnimation(AzBakedAnimation animation, AzPlayBehavior playBehavior) {
		this.animation = animation;
		this.playBehavior = playBehavior;
	}

	public AzBakedAnimation animation() {
		return animation;
	}

	public AzPlayBehavior playBehavior() {
		return playBehavior;
	}

	@Override
	public String toString() {
		return "AzQueuedAnimation{" +
			       "animation=" + animation +
			       ", playBehavior=" + playBehavior +
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

		AzQueuedAnimation that = (AzQueuedAnimation) o;

		if (!animation.equals(that.animation)) {
			return false;
		}
		return playBehavior.equals(that.playBehavior);
	}

	@Override
	public int hashCode() {
		int result = animation.hashCode();
		result = 31 * result + playBehavior.hashCode();
		return result;
	}
}