package mod.azure.azurelib.animation;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.pose.AzPoseLayer;
import mod.azure.azurelib.core.molang.MolangScope;

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

    private final MolangScope molangScope = new MolangScope();

    private final List<AzAnimator<?, ?>> linkedAnimators = new ArrayList<>(0);

    private final Map<Object, Object> attachments = new IdentityHashMap<>(0);

    private final AzPoseLayer<T> poseLayer;

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
        this.poseLayer = new AzPoseLayer<>(this);
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

    /**
     * This animatable's Molang {@code variable.*} values. Animations read and write them while this animatable is
     * animated; code can set them too, e.g. {@code context.molangScope().set("v.charge", charge)}, to drive an
     * animation per animatable without registering a global variable.
     *
     * @return the Molang variable scope of this animatable
     */
    public MolangScope molangScope() {
        return molangScope;
    }

    /**
     * Animators that follow this one. Every animation command dispatched to this animatable's animator is also applied
     * to each linked animator, with the same origin side. Used by animated render layer overlays (see
     * {@code AzRenderLayer#reRenderWithAnimatedBakedModel}).
     *
     * @return the mutable list of linked animators
     */
    public List<AzAnimator<?, ?>> linkedAnimators() {
        return linkedAnimators;
    }

    /**
     * This animatable's procedural pose state: the live weight of each pose modifier (see
     * {@link AzAnimator#registerPoseModifiers}), their per-animatable state, and the bookkeeping that keeps procedural
     * offsets out of the animated pose. Use it to switch or fade modifiers per animatable, e.g.
     * {@code context.poseLayer().fadeWeight("look", 0, 10)} while the entity is attacking.
     *
     * @return the pose layer of this animatable
     */
    public AzPoseLayer<T> poseLayer() {
        return poseLayer;
    }

    /**
     * Per-animatable storage for render layers and addons, keyed by identity. Entries live exactly as long as this
     * context, so they never need weak references back to the animatable.
     *
     * @return the mutable attachment map
     */
    public Map<Object, Object> attachments() {
        return attachments;
    }
}
