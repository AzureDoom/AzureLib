package mod.azure.azurelib.animation.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzBoneAnimationUpdateUtil;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimation;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimationQueue;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * The AzBoneAnimationQueueCache class is responsible for managing and updating animation queues for bones. It acts as a
 * cache that maps bone names to their respective animation queues, enabling efficient updates and access.
 *
 * @param <T> the type of the animatable object used in the animation context
 */
@SuppressWarnings("unused")
public class AzBoneAnimationQueueCache<T> {

    private static final AzBoneAnimationQueue[] NO_QUEUES = new AzBoneAnimationQueue[0];

    private final Map<String, AzBoneAnimationQueue> boneAnimationQueues;

    private final AzBoneCache boneCache;

    @Nullable
    private AzBakedAnimation resolvedAnimation;

    @Nullable
    private AzBakedModel resolvedModel;

    private AzBoneAnimationQueue[] resolvedQueues = NO_QUEUES;

    public AzBoneAnimationQueueCache(AzBoneCache boneCache) {
        this.boneAnimationQueues = new HashMap<>();
        this.boneCache = boneCache;
    }

    /**
     * Updates the animations of all bones in the cache by applying transformations such as rotation, position, and
     * scale based on the specified easing type. The method retrieves current bone snapshots and initial snapshots to
     * calculate the updated transformations for each bone animation queue.
     *
     * @param easingType the easing type used for calculating the interpolation of transformations such as rotation,
     *                   position, and scale
     */
    public void update(AzEasingType easingType) {
        update(easingType, 1, AzBlendMode.OVERRIDE);
    }

    /**
     * Applies this frame's values to the bones, combined with what is already there by {@code blendMode} and
     * {@code weight}. See {@link AzBoneAnimationUpdateUtil} for how controllers layer.
     */
    public void update(AzEasingType easingType, double weight, AzBlendMode blendMode) {
        Map<String, AzBoneSnapshot> boneSnapshots = boneCache.getBoneSnapshotsByName();
        long frame = boneCache.currentFrame();

        for (AzBoneAnimationQueue boneAnimation : boneAnimationQueues.values()) {
            AzBone bone = boneAnimation.bone();
            AzBoneSnapshot snapshot = boneSnapshots.get(bone.getName());
            AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();

            AzBoneAnimationUpdateUtil.updateRotations(
                boneAnimation,
                bone,
                easingType,
                initialSnapshot,
                snapshot,
                weight,
                blendMode,
                frame
            );
            AzBoneAnimationUpdateUtil.updatePositions(
                boneAnimation,
                bone,
                easingType,
                initialSnapshot,
                snapshot,
                weight,
                blendMode,
                frame
            );
            AzBoneAnimationUpdateUtil.updateScale(
                boneAnimation,
                bone,
                easingType,
                initialSnapshot,
                snapshot,
                weight,
                blendMode,
                frame
            );
        }
    }

    public Collection<AzBoneAnimationQueue> values() {
        return boneAnimationQueues.values();
    }

    /**
     * Retrieves the animation queue for the specified bone name or returns null if the bone does not exist.
     *
     * @param boneName the name of the bone for which the animation queue is to be retrieved
     * @return the {@code AzBoneAnimationQueue} associated with the specified bone name, or {@code null} if the bone
     *         does not exist
     */
    public @Nullable AzBoneAnimationQueue getOrNull(String boneName) {
        AzBone bone = boneCache.getBakedModel().getBoneOrNull(boneName);

        if (bone == null) {
            return null;
        }

        AzBoneAnimationQueue queue = boneAnimationQueues.get(boneName);

        if (queue == null) {
            queue = new AzBoneAnimationQueue(bone);
            boneAnimationQueues.put(boneName, queue);
        }

        return queue;
    }

    /**
     * Clears all the animation queues stored in the cache. This method removes all mappings of bone names to their
     * respective {@code AzBoneAnimationQueue} objects, effectively resetting the cache to an empty state.
     */
    public void prepareFrame() {
        for (AzBoneAnimationQueue queue : boneAnimationQueues.values()) {
            queue.clearFrame();
        }
    }

    /**
     * Returns the animation queue for each of {@code animation}'s bone animations, by the same index as
     * {@link AzBakedAnimation#boneAnimations()}, with {@code null} for bones the current model doesn't have.
     * <p>
     * Resolving by name costs two hash lookups per bone, so the result is cached and reused for as long as the same
     * animation plays on the same baked model. Only a different animation, a model change or {@link #clear()} resolves
     * again. The returned array is shared; don't modify it.
     * </p>
     */
    public AzBoneAnimationQueue[] resolveQueues(AzBakedAnimation animation) {
        AzBakedModel model = boneCache.getBakedModel();

        if (animation != resolvedAnimation || model != resolvedModel) {
            AzBoneAnimation[] boneAnimations = animation.boneAnimations();
            AzBoneAnimationQueue[] queues = new AzBoneAnimationQueue[boneAnimations.length];

            for (int i = 0; i < boneAnimations.length; i++) {
                queues[i] = getOrNull(boneAnimations[i].boneName());
            }

            resolvedQueues = queues;
            resolvedAnimation = animation;
            resolvedModel = model;
        }

        return resolvedQueues;
    }

    public void clear() {
        boneAnimationQueues.clear();
        resolvedAnimation = null;
        resolvedModel = null;
        resolvedQueues = NO_QUEUES;
    }
}
