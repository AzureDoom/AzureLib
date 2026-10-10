/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.event;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.core.keyframe.event.data.SoundKeyframeData;

/**
 * The {@link AzKeyframeEvent} specific to the {@link AzKeyframeCallbacks#soundKeyframeHandler()}.<br>
 * Called when a sound instruction keyframe is encountered
 */
public class AzSoundKeyframeEvent<T> extends AzKeyframeEvent<T, SoundKeyframeData> {

    /**
     * This stores all the fields that are needed in the AnimationTestEvent
     *
     * @param entity        the entity
     * @param animationTick The amount of ticks that have passed in either the current transition or animation,
     *                      depending on the controller's AnimationState.
     * @param controller    the controller
     */
    public AzSoundKeyframeEvent(
        T entity,
        double animationTick,
        AzAnimationController<T> controller,
        SoundKeyframeData keyframeData
    ) {
        super(entity, animationTick, controller, keyframeData);
    }
}
