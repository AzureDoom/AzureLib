package mod.azure.azurelib.core.animation;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.Animation.LoopType;

/**
 * {@link Animation} and {@link mod.azure.azurelib.core.animation.Animation.LoopType} override pair, used to define a playable animation stage for a {@link GeoAnimatable}
 */
public class QueuedAnimation {
	public Animation animation;
	public Animation.LoopType loopType;

	public QueuedAnimation(Animation animation, LoopType loopType) {
		this.animation = animation;
		this.loopType = loopType;
	}

	public Animation animation() {
		return this.animation;
	}

	public Animation.LoopType loopType() {
		return this.loopType;
	}
}
