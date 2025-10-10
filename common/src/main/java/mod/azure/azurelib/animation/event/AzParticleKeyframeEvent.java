/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.event;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.AzKeyframeCallbacks;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;

/**
 * The {@link AzKeyframeEvent} specific to the {@link AzKeyframeCallbacks#particleKeyframeHandler()}.<br>
 * Called when a particle instruction keyframe is encountered
 */
public class AzParticleKeyframeEvent<T> extends AzKeyframeEvent<T, ParticleKeyframeData> {

    public AzParticleKeyframeEvent(
        T animatable,
        double animationTick,
        AzAnimationController<T> controller,
        ParticleKeyframeData particleKeyframeData
    ) {
        super(animatable, animationTick, controller, particleKeyframeData);
    }

    /**
     * Get the {@link ParticleKeyframeData} relevant to this event call
     */
    @Override
    public ParticleKeyframeData getKeyframeData() {
        return super.getKeyframeData();
    }
}
