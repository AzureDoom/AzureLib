package mod.azure.azurelib.animation.controller.keyframe.handler;

import mod.azure.azurelib.animation.event.AzParticleKeyframeEvent;

/**
 * A handler for when a predefined particle keyframe is hit. When the keyframe is encountered, the
 * {@link AzParticleKeyframeHandler#handle(AzParticleKeyframeEvent)} method will be called. Spawn the particles/effects
 * of your choice at this time.
 */
@FunctionalInterface
public interface AzParticleKeyframeHandler<A> {

    void handle(AzParticleKeyframeEvent<A> event);
}
