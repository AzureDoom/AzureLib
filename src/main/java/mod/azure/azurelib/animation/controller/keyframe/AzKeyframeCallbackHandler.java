package mod.azure.azurelib.animation.controller.keyframe;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.keyframe.handler.AzCustomKeyframeHandler;
import mod.azure.azurelib.animation.controller.keyframe.handler.AzParticleKeyframeHandler;
import mod.azure.azurelib.animation.controller.keyframe.handler.AzSoundKeyframeHandler;
import mod.azure.azurelib.animation.event.AzCustomInstructionKeyframeEvent;
import mod.azure.azurelib.animation.event.AzParticleKeyframeEvent;
import mod.azure.azurelib.animation.event.AzSoundKeyframeEvent;
import mod.azure.azurelib.animation.primitive.AzKeyframes;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.core.keyframe.event.data.CustomInstructionKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.KeyFrameData;
import mod.azure.azurelib.core.keyframe.event.data.ParticleKeyframeData;
import mod.azure.azurelib.core.keyframe.event.data.SoundKeyframeData;

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

    private static final Logger LOGGER = LogManager.getLogger(AzKeyframeCallbackHandler.class);

    private final AzAnimationController<T> animationController;

    private final Set<KeyFrameData> executedKeyframes;

    private final AzKeyframeCallbacks<T> keyframeCallbacks;

    private boolean reversed;

    public AzKeyframeCallbackHandler(
        AzAnimationController<T> animationController,
        AzKeyframeCallbacks<T> keyframeCallbacks
    ) {
        this.animationController = animationController;
        this.executedKeyframes = new HashSet<>();
        this.keyframeCallbacks = keyframeCallbacks;
    }

    public void handle(T animatable, double adjustedTick) {
        handle(animatable, adjustedTick, false);
    }

    public void handle(T animatable, double sampleTick, boolean reversed) {
        this.reversed = reversed;
        handleSoundKeyframes(animatable, sampleTick);
        handleParticleKeyframes(animatable, sampleTick);
        handleCustomKeyframes(animatable, sampleTick);
    }

    private boolean reached(KeyFrameData keyframeData, double sampleTick) {
        return reversed ? sampleTick <= keyframeData.getStartTick() : sampleTick >= keyframeData.getStartTick();
    }

    private void handleCustomKeyframes(T animatable, double adjustedTick) {
        AzCustomKeyframeHandler<T> customKeyframeHandler = keyframeCallbacks.customKeyframeHandler();
        CustomInstructionKeyframeData[] customInstructions = currentAnimation().animation()
            .keyframes()
            .customInstructions();

        for (CustomInstructionKeyframeData keyframeData : customInstructions) {
            if (reached(keyframeData, adjustedTick) && executedKeyframes.add(keyframeData)) {
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
        AzParticleKeyframeHandler<T> particleKeyframeHandler = keyframeCallbacks.particleKeyframeHandler();
        ParticleKeyframeData[] particleInstructions = currentAnimation().animation().keyframes().particles();

        for (ParticleKeyframeData keyframeData : particleInstructions) {
            if (reached(keyframeData, adjustedTick) && executedKeyframes.add(keyframeData)) {
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
        AzSoundKeyframeHandler<T> soundKeyframeHandler = keyframeCallbacks.soundKeyframeHandler();
        SoundKeyframeData[] soundInstructions = currentAnimation().animation().keyframes().sounds();

        for (SoundKeyframeData keyframeData : soundInstructions) {
            if (reached(keyframeData, adjustedTick) && executedKeyframes.add(keyframeData)) {
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

    public void resync(double sampleTick, boolean reversed) {
        this.reversed = reversed;
        executedKeyframes.clear();

        AzQueuedAnimation current = currentAnimation();

        if (current == null) {
            return;
        }

        AzKeyframes keyframes = current.animation().keyframes();

        markReached(keyframes.sounds(), sampleTick);
        markReached(keyframes.particles(), sampleTick);
        markReached(keyframes.customInstructions(), sampleTick);
    }

    private void markReached(KeyFrameData[] keyframes, double sampleTick) {
        for (KeyFrameData keyframeData : keyframes) {
            if (reached(keyframeData, sampleTick)) {
                executedKeyframes.add(keyframeData);
            }
        }
    }

    private AzQueuedAnimation currentAnimation() {
        return animationController.currentAnimation();
    }
}
