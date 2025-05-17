package mod.azure.azurelib.animation.controller.keyframe;

import java.util.Map;
import java.util.NoSuchElementException;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.animation.controller.AzBoneSnapshotCache;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.object.Axis;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * AzKeyframeTransitioner is a specialized class for executing smooth animations and transitions between keyframes for
 * bones in an animation system. It utilizes animation controllers, bone animation queue caches, and bone snapshot
 * caches to manage and apply transitions for rotation, position, and scale of bones.
 *
 * @param <T> The type of the animation data handled by the associated animation controller.
 */
public class AzKeyframeTransitioner<T> extends AzAbstractKeyframeExecutor {

    private final AzAnimationController<T> animationController;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    private final AzBoneSnapshotCache boneSnapshotCache;

    public AzKeyframeTransitioner(
        AzAnimationController<T> animationController,
        AzBoneAnimationQueueCache<T> boneAnimationQueueCache,
        AzBoneSnapshotCache boneSnapshotCache
    ) {
        this.animationController = animationController;
        this.boneAnimationQueueCache = boneAnimationQueueCache;
        this.boneSnapshotCache = boneSnapshotCache;
    }

    public void transition(Map<String, AzBone> bones, boolean crashWhenCantFindBone, double adjustedTick) {
        AzQueuedAnimation currentAnimation = animationController.currentAnimation();
        float transitionLength = animationController.animationProperties().transitionLength();

        MolangParser.INSTANCE.setValue(MolangQueries.ANIM_TIME, () -> 0);

        for (AzBoneAnimation boneAnimation : currentAnimation.animation().boneAnimations()) {
            AzBone bone = bones.get(boneAnimation.boneName());

            if (bone == null) {
                if (crashWhenCantFindBone)
                    throw new NoSuchElementException("Could not find bone: " + boneAnimation.boneName());

                continue;
            }

            AzBoneAnimationQueue queue = boneAnimationQueueCache.getOrNull(boneAnimation.boneName());
            AzBoneSnapshot snapshot = boneSnapshotCache.getOrNull(boneAnimation.boneName());

            AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes = boneAnimation.rotationKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes = boneAnimation.positionKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes = boneAnimation.scaleKeyframes();

            transitionRotation(adjustedTick, rotationKeyframes, queue, transitionLength, snapshot, bone);
            transitionPosition(adjustedTick, positionKeyframes, queue, transitionLength, snapshot);
            transitionScale(adjustedTick, scaleKeyframes, queue, transitionLength, snapshot);
        }
    }

    private void transitionRotation(
        double adjustedTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot,
        AzBone bone
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzBoneSnapshot initialSnapshot = bone.getInitialAzSnapshot();
        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), 0, true, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), 0, true, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), 0, true, Axis.Z);

        queue.addNextRotation(null, adjustedTick, transitionLength, snapshot, initialSnapshot, x, y, z);
    }

    private void transitionPosition(
        double adjustedTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), 0, false, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), 0, false, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), 0, false, Axis.Z);

        queue.addNextPosition(null, adjustedTick, transitionLength, snapshot, x, y, z);
    }

    private void transitionScale(
        double adjustedTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), 0, false, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), 0, false, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), 0, false, Axis.Z);

        queue.addNextScale(null, adjustedTick, transitionLength, snapshot, x, y, z);
    }
}
