package mod.azure.azurelib.rewrite.animation.controller.keyframe;

import mod.azure.azurelib.rewrite.animation.controller.keyframe.handler.AzCustomKeyframeHandler;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.handler.AzParticleKeyframeHandler;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.handler.AzSoundKeyframeHandler;
import mod.azure.azurelib.rewrite.animation.event.AzCustomInstructionKeyframeEvent;
import mod.azure.azurelib.rewrite.animation.event.AzParticleKeyframeEvent;
import mod.azure.azurelib.rewrite.animation.event.AzSoundKeyframeEvent;

/**
 * The AzKeyframeCallbacks class manages callbacks for different types of keyframe events, enabling the handling of
 * sound, particle, and custom-defined keyframe instructions during an animation sequence.
 *
 * @param <T> The type of entity or object this keyframe callback interacts with.
 */
public class AzKeyframeCallbacks<T> {

    private static final AzKeyframeCallbacks<?> NO_OP = new AzKeyframeCallbacks<>(null, null, null);

    @SuppressWarnings("unchecked")
    public static <T> AzKeyframeCallbacks<T> noop() {
        return (AzKeyframeCallbacks<T>) NO_OP;
    }

    private final AzCustomKeyframeHandler<T> customKeyframeHandler;

    private final AzParticleKeyframeHandler<T> particleKeyframeHandler;

    private final AzSoundKeyframeHandler<T> soundKeyframeHandler;

    private AzKeyframeCallbacks(
        AzCustomKeyframeHandler<T> customKeyframeHandler,
        AzParticleKeyframeHandler<T> particleKeyframeHandler,
        AzSoundKeyframeHandler<T> soundKeyframeHandler
    ) {
        this.customKeyframeHandler = customKeyframeHandler;
        this.particleKeyframeHandler = particleKeyframeHandler;
        this.soundKeyframeHandler = soundKeyframeHandler;
    }

    public AzCustomKeyframeHandler<T> customKeyframeHandler() {
        return customKeyframeHandler;
    }

    public AzParticleKeyframeHandler<T> particleKeyframeHandler() {
        return particleKeyframeHandler;
    }

    public AzSoundKeyframeHandler<T> soundKeyframeHandler() {
        return soundKeyframeHandler;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {

        private AzCustomKeyframeHandler<T> customKeyframeHandler;

        private AzParticleKeyframeHandler<T> particleKeyframeHandler;

        private AzSoundKeyframeHandler<T> soundKeyframeHandler;

        private Builder() {}

        /**
         * Applies the given {@link AzSoundKeyframeHandler} to this controller, for handling {@link AzSoundKeyframeEvent
         * sound keyframe instructions}.
         *
         * @return this
         */
        public Builder<T> setSoundKeyframeHandler(AzSoundKeyframeHandler<T> soundHandler) {
            this.soundKeyframeHandler = soundHandler;
            return this;
        }

        /**
         * Applies the given {@link AzParticleKeyframeHandler} to this controller, for handling
         * {@link AzParticleKeyframeEvent particle keyframe instructions}.
         *
         * @return this
         */
        public Builder<T> setParticleKeyframeHandler(AzParticleKeyframeHandler<T> particleHandler) {
            this.particleKeyframeHandler = particleHandler;
            return this;
        }

        /**
         * Applies the given {@link AzCustomKeyframeHandler} to this controller, for handling
         * {@link AzCustomInstructionKeyframeEvent sound keyframe instructions}.
         *
         * @return this
         */
        public Builder<T> setCustomInstructionKeyframeHandler(AzCustomKeyframeHandler<T> customInstructionHandler) {
            this.customKeyframeHandler = customInstructionHandler;
            return this;
        }

        public AzKeyframeCallbacks<T> build() {
            return new AzKeyframeCallbacks<>(customKeyframeHandler, particleKeyframeHandler, soundKeyframeHandler);
        }
    }
}
