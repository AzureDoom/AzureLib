package mod.azure.azurelib.animation.controller.keyframe;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.animation.controller.AzBoneSnapshotCache;

/**
 * AzKeyframeManager is responsible for managing the keyframe-related operations in an animation system. It coordinates
 * the execution, transition, and callback handling of animation keyframes through its associated components.
 *
 * @param <T> the type of the animatable object being handled
 */
public class AzKeyframeManager<T> {

    private final AzKeyframeCallbackHandler<T> keyframeCallbackHandler;

    private final AzKeyframeExecutor<T> keyframeExecutor;

    private final AzKeyframeTransitioner<T> keyframeTransitioner;

    public AzKeyframeManager(
        AzAnimationController<T> animationController,
        AzBoneAnimationQueueCache<T> boneAnimationQueueCache,
        AzBoneSnapshotCache boneSnapshotCache,
        AzKeyframeCallbacks<T> keyframeCallbacks
    ) {
        this.keyframeCallbackHandler = new AzKeyframeCallbackHandler<>(animationController, keyframeCallbacks);
        this.keyframeExecutor = new AzKeyframeExecutor<>(animationController, boneAnimationQueueCache);
        this.keyframeTransitioner = new AzKeyframeTransitioner<>(
            animationController,
            boneAnimationQueueCache,
            boneSnapshotCache
        );
    }

    public AzKeyframeCallbackHandler<T> keyframeCallbackHandler() {
        return keyframeCallbackHandler;
    }

    public AzKeyframeExecutor<T> keyframeExecutor() {
        return keyframeExecutor;
    }

    public AzKeyframeTransitioner<T> keyframeTransitioner() {
        return keyframeTransitioner;
    }
}
