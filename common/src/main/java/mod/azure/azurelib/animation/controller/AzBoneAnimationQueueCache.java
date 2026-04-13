package mod.azure.azurelib.animation.controller;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

import mod.azure.azurelib.animation.AzBoneAnimationUpdateUtil;
import mod.azure.azurelib.animation.cache.AzBoneCache;
import mod.azure.azurelib.animation.controller.keyframe.AzBoneAnimationQueue;
import mod.azure.azurelib.animation.easing.AzEasingType;

/**
 * The AzBoneAnimationQueueCache class is responsible for managing and updating animation queues for bones. It acts as a
 * cache that maps bone names to their respective animation queues, enabling efficient updates and access.
 *
 * @param <T> the type of the animatable object used in the animation context
 */
public class AzBoneAnimationQueueCache<T> {

    private final Map<String, AzBoneAnimationQueue> boneAnimationQueues;

    private final AzBoneCache boneCache;

    public AzBoneAnimationQueueCache(AzBoneCache boneCache) {
        this.boneAnimationQueues = new Object2ObjectOpenHashMap<>();
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
        var boneSnapshots = boneCache.getBoneSnapshotsByName();

        for (var boneAnimation : boneAnimationQueues.values()) {
            var bone = boneAnimation.bone();
            var snapshot = boneSnapshots.get(bone.getName());
            var initialSnapshot = bone.getInitialAzSnapshot();

            AzBoneAnimationUpdateUtil.updateRotations(boneAnimation, bone, easingType, initialSnapshot, snapshot);
            AzBoneAnimationUpdateUtil.updatePositions(boneAnimation, bone, easingType, snapshot);
            AzBoneAnimationUpdateUtil.updateScale(boneAnimation, bone, easingType, snapshot);
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
        var bone = boneCache.getBakedModel().getBoneOrNull(boneName);

        if (bone == null) {
            return null;
        }

        return boneAnimationQueues.computeIfAbsent(boneName, $ -> new AzBoneAnimationQueue(bone));
    }

    /**
     * Clears all the animation queues stored in the cache. This method removes all mappings of bone names to their
     * respective {@code AzBoneAnimationQueue} objects, effectively resetting the cache to an empty state.
     */
    public void clear() {
        boneAnimationQueues.clear();
    }
}
