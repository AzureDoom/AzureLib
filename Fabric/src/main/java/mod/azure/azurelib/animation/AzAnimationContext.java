package mod.azure.azurelib.animation;

import mod.azure.azurelib.animation.cache.AzBoneCache;

/**
 * The {@code AzAnimationContext} class provides a context for managing animation-related state and behaviors for
 * animatable objects of type {@code T}. It serves as the central point of interaction between the animation system,
 * configuration settings, and the animated object itself.
 *
 * @param <T> The type of the animatable object that this context operates on.
 */
public class AzAnimationContext<T> {

    private final AzBoneCache boneCache;

    private final AzAnimatorConfig config;

    private final AzAnimationTimer timer;

    // Package-private for mutability purposes.
    T animatable;

    public AzAnimationContext(
        AzBoneCache boneCache,
        AzAnimatorConfig config,
        AzAnimationTimer timer
    ) {
        this.boneCache = boneCache;
        this.config = config;
        this.timer = timer;
    }

    /**
     * Returns the current animatable instance associated with this animation context.
     *
     * @return The animatable instance of type {@code T}.
     */
    public T animatable() {
        return animatable;
    }

    /**
     * Returns the bone cache associated with the animation context. The bone cache is responsible for storing and
     * managing bone-related data and transformations used during animations.
     *
     * @return The {@link AzBoneCache} instance managing bone data and transformations for animations.
     */
    public AzBoneCache boneCache() {
        return boneCache;
    }

    /**
     * Returns the animation configuration associated with this animation context. The configuration defines behavior
     * such as bone reset speed, handling of missing bones, and whether animations should play while the game is paused.
     *
     * @return The {@link AzAnimatorConfig} instance containing the animation settings for this context.
     */
    public AzAnimatorConfig config() {
        return config;
    }

    /**
     * Returns the animation timer associated with the animation context. The timer is used to track animation progress
     * over time and manage timing adjustments based on game state, such as pausing and resuming.
     *
     * @return The {@link AzAnimationTimer} instance responsible for animation timing.
     */
    public AzAnimationTimer timer() {
        return timer;
    }
}
