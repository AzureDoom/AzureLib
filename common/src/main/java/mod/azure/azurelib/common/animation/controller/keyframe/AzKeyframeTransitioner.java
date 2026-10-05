package mod.azure.azurelib.common.animation.controller.keyframe;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.common.animation.controller.AzBoneSnapshotCache;
import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.model.AzBoneSnapshot;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.core.object.Axis;

/**
 * AzKeyframeTransitioner is a specialized class for executing smooth animations and transitions between keyframes for
 * bones in an animation system. It utilizes animation controllers, bone animation queue caches, and bone snapshot
 * caches to manage and apply transitions for rotation, position, and scale of bones.
 *
 * @param <T> The type of the animation data handled by the associated animation controller.
 */
public class AzKeyframeTransitioner<T> extends AzAbstractKeyframeExecutor {

    private static final MolangVariableRef ANIM_TIME_REF = new MolangVariableRef(MolangQueries.ANIM_TIME);

    private final AzAnimationController<T> animationController;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    private final AzBoneSnapshotCache boneSnapshotCache;

    private double currentAdjustedTick;

    private final DoubleSupplier animTimeSupplier = () -> currentAdjustedTick / 20d;

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
        var currentAnimation = animationController.currentAnimation();
        var transitionLength = animationController.animationProperties().transitionLength();
        adjustedTick = Math.min(adjustedTick, transitionLength); // Cap tick length

        var targetTick = animationController.isPlayingReversed() ? currentAnimation.animation().length() : 0D;

        currentAdjustedTick = targetTick;
        ANIM_TIME_REF.setMemoized(animTimeSupplier);

        for (var boneAnimation : currentAnimation.animation().boneAnimations()) {
            var bone = bones.get(boneAnimation.boneName());

            if (bone == null) {
                if (crashWhenCantFindBone)
                    throw new NoSuchElementException("Could not find bone: " + boneAnimation.boneName());

                continue;
            }

            // Bones outside the controller's mask are skipped before their keyframes are evaluated.
            if (!animationController.boneMask().includes(bone)) {
                continue;
            }

            var queue = boneAnimationQueueCache.getOrNull(boneAnimation.boneName());
            var snapshot = boneSnapshotCache.getOrNull(boneAnimation.boneName());

            if (snapshot == null || queue == null) {
                return;
            }

            var rotationKeyframes = boneAnimation.rotationKeyframes();
            var positionKeyframes = boneAnimation.positionKeyframes();
            var scaleKeyframes = boneAnimation.scaleKeyframes();

            transitionRotation(adjustedTick, targetTick, rotationKeyframes, queue, transitionLength, snapshot, bone);
            transitionPosition(adjustedTick, targetTick, positionKeyframes, queue, transitionLength, snapshot);
            transitionScale(adjustedTick, targetTick, scaleKeyframes, queue, transitionLength, snapshot);
        }
    }

    private void transitionRotation(
        double adjustedTick,
        double targetTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot,
        AzBone bone
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        var initialSnapshot = bone.getInitialAzSnapshot();
        var x = getAnimationPointAtTick(keyframes.xKeyframes(), targetTick, true, Axis.X);
        var y = getAnimationPointAtTick(keyframes.yKeyframes(), targetTick, true, Axis.Y);
        var z = getAnimationPointAtTick(keyframes.zKeyframes(), targetTick, true, Axis.Z);

        queue.addNextRotation(null, adjustedTick, transitionLength, snapshot, initialSnapshot, x, y, z);
    }

    private void transitionPosition(
        double adjustedTick,
        double targetTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot
    ) {
        var x = getAnimationPointAtTick(keyframes.xKeyframes(), targetTick, false, Axis.X);
        var y = getAnimationPointAtTick(keyframes.yKeyframes(), targetTick, false, Axis.Y);
        var z = getAnimationPointAtTick(keyframes.zKeyframes(), targetTick, false, Axis.Z);
        queue.addNextPosition(null, adjustedTick, transitionLength, snapshot, x, y, z);
    }

    private void transitionScale(
        double adjustedTick,
        double targetTick,
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double transitionLength,
        AzBoneSnapshot snapshot
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        var x = getAnimationPointAtTick(keyframes.xKeyframes(), targetTick, false, Axis.X);
        var y = getAnimationPointAtTick(keyframes.yKeyframes(), targetTick, false, Axis.Y);
        var z = getAnimationPointAtTick(keyframes.zKeyframes(), targetTick, false, Axis.Z);

        queue.addNextScale(null, adjustedTick, transitionLength, snapshot, x, y, z);
    }
}
