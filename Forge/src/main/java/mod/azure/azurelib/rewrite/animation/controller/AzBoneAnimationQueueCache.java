package mod.azure.azurelib.rewrite.animation.controller;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Collection;
import java.util.Map;

import mod.azure.azurelib.rewrite.animation.AzBoneAnimationUpdateUtil;
import mod.azure.azurelib.rewrite.animation.cache.AzBoneCache;
import mod.azure.azurelib.rewrite.animation.controller.keyframe.AzBoneAnimationQueue;
import mod.azure.azurelib.rewrite.animation.easing.AzEasingType;
import mod.azure.azurelib.rewrite.model.AzBone;
import mod.azure.azurelib.rewrite.model.AzBoneSnapshot;

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

    public void update(AzEasingType easingType) {
        Map<String, AzBoneSnapshot> boneSnapshots = boneCache.getBoneSnapshotsByName();

        for (AzBoneAnimationQueue boneAnimation : boneAnimationQueues.values()) {
            AzBone bone = boneAnimation.bone();
            AzBoneSnapshot snapshot = boneSnapshots.get(bone.getName());
            AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();

            AzBoneAnimationUpdateUtil.updateRotations(boneAnimation, bone, easingType, initialSnapshot, snapshot);
            AzBoneAnimationUpdateUtil.updatePositions(boneAnimation, bone, easingType, snapshot);
            AzBoneAnimationUpdateUtil.updateScale(boneAnimation, bone, easingType, snapshot);
        }
    }

    public Collection<AzBoneAnimationQueue> values() {
        return boneAnimationQueues.values();
    }

    public AzBoneAnimationQueue getOrNull(String boneName) {
        AzBone bone = boneCache.getBakedModel().getBoneOrNull(boneName);

        if (bone == null) {
            return null;
        }

        return boneAnimationQueues.computeIfAbsent(boneName, $ -> new AzBoneAnimationQueue(bone));
    }

    public void clear() {
        boneAnimationQueues.clear();
    }
}
