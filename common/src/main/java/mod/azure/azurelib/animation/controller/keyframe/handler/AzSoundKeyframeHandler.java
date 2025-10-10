package mod.azure.azurelib.animation.controller.keyframe.handler;

import mod.azure.azurelib.animation.event.AzSoundKeyframeEvent;

/**
 * A handler for when a predefined sound keyframe is hit. When the keyframe is encountered, the
 * {@link AzSoundKeyframeHandler#handle(AzSoundKeyframeEvent)} method will be called. Play the sound(s) of your choice
 * at this time.
 */
@FunctionalInterface
public interface AzSoundKeyframeHandler<A> {

    void handle(AzSoundKeyframeEvent<A> event);
}
