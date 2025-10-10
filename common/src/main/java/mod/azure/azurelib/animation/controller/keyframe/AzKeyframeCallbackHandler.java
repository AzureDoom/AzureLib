package mod.azure.azurelib.animation.controller.keyframe;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.event.AzCustomInstructionKeyframeEvent;
import mod.azure.azurelib.animation.event.AzParticleKeyframeEvent;
import mod.azure.azurelib.animation.event.AzSoundKeyframeEvent;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.core.keyframe.event.data.KeyFrameData;

/**
 * AzKeyframeCallbackHandler acts as a handler for managing animation keyframe events such as sound, particle, or custom
 * events during a specific animation. It works in conjunction with an animation controller and a set of keyframe
 * callbacks, executing them as appropriate based on the animation's progress. <br>
 * This class is generic and operates on a user-defined animatable type to handle various keyframe events related to
 * animations.
 *
 * @param <T> the type of the animatable object being handled
 */
// TODO: reduce the boilerplate of the specialized handle functions in this class.
public class AzKeyframeCallbackHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzKeyframeCallbackHandler.class);

    private final AzAnimationController<T> animationController;

    private final Set<KeyFrameData> executedKeyframes;

    private final AzKeyframeCallbacks<T> keyframeCallbacks;

    public AzKeyframeCallbackHandler(
        AzAnimationController<T> animationController,
        AzKeyframeCallbacks<T> keyframeCallbacks
    ) {
        this.animationController = animationController;
        this.executedKeyframes = new ObjectOpenHashSet<>();
        this.keyframeCallbacks = keyframeCallbacks;
    }

    public void handle(T animatable, double adjustedTick) {
        handleSoundKeyframes(animatable, adjustedTick);
        handleParticleKeyframes(animatable, adjustedTick);
        handleCustomKeyframes(animatable, adjustedTick);
    }

    private void handleCustomKeyframes(T animatable, double adjustedTick) {
        var customKeyframeHandler = keyframeCallbacks.customKeyframeHandler();
        var customInstructions = currentAnimation().animation().keyframes().customInstructions();

        for (var keyframeData : customInstructions) {
            if (adjustedTick >= keyframeData.getStartTick() && executedKeyframes.add(keyframeData)) {
                if (customKeyframeHandler == null) {
                    LOGGER.warn(
                        "Custom Instruction Keyframe found for {} -> {}, but no keyframe handler registered",
                        animatable.getClass().getSimpleName(),
                        animationController.name()
                    );
                    break;
                }

                customKeyframeHandler.handle(
                    new AzCustomInstructionKeyframeEvent<>(animatable, adjustedTick, animationController, keyframeData)
                );
            }
        }
    }

    private void handleParticleKeyframes(T animatable, double adjustedTick) {
        var particleKeyframeHandler = keyframeCallbacks.particleKeyframeHandler();
        var particleInstructions = currentAnimation().animation().keyframes().particles();

        for (var keyframeData : particleInstructions) {
            if (adjustedTick >= keyframeData.getStartTick() && executedKeyframes.add(keyframeData)) {
                if (particleKeyframeHandler == null) {
                    LOGGER.warn(
                        "Particle Keyframe found for {} -> {}, but no keyframe handler registered",
                        animatable.getClass().getSimpleName(),
                        animationController.name()
                    );
                    break;
                }

                particleKeyframeHandler.handle(
                    new AzParticleKeyframeEvent<>(animatable, adjustedTick, animationController, keyframeData)
                );
            }
        }
    }

    private void handleSoundKeyframes(T animatable, double adjustedTick) {
        var soundKeyframeHandler = keyframeCallbacks.soundKeyframeHandler();
        var soundInstructions = currentAnimation().animation().keyframes().sounds();

        for (var keyframeData : soundInstructions) {
            if (adjustedTick >= keyframeData.getStartTick() && executedKeyframes.add(keyframeData)) {
                if (soundKeyframeHandler == null) {
                    LOGGER.warn(
                        "Sound Keyframe found for {} -> {}, but no keyframe handler registered",
                        animatable.getClass().getSimpleName(),
                        animationController.name()
                    );
                    break;
                }

                soundKeyframeHandler.handle(
                    new AzSoundKeyframeEvent<>(animatable, adjustedTick, animationController, keyframeData)
                );
            }
        }
    }

    /**
     * Clear the {@link KeyFrameData} cache in preparation for the next animation
     */
    public void reset() {
        executedKeyframes.clear();
    }

    private AzQueuedAnimation currentAnimation() {
        return animationController.currentAnimation();
    }
}
