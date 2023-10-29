package mod.azure.azurelib.core.keyframe.event;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;

/**
 * The {@link KeyFrameEvent} specific to the {@link AnimationController#particleKeyframeHandler}.<br>
 * Called when a particle instruction keyframe is encountered
 */
public class ParticleKeyframeEvent<T extends GeoAnimatable> extends KeyFrameEvent<T, ParticleKeyframeData> {
	public ParticleKeyframeEvent(T animatable, double animationTick, AnimationController<T> controller, ParticleKeyframeData particleKeyFrameData) {
		super(animatable, animationTick, controller, particleKeyFrameData);
	}
}
