package mod.azure.azurelib.common.animation.controller.keyframe;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.common.animation.controller.AzAnimationController;
import mod.azure.azurelib.common.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.common.animation.controller.AzBoneSnapshotCache;
import mod.azure.azurelib.common.animation.easing.AzEasingType;
import mod.azure.azurelib.common.model.AzBone;
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

    private int[] cursors;

    private AzEasingType easingOverride;

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

        var animation = currentAnimation.animation();

        currentAdjustedTick = animationController.isPlayingReversed() ? animation.length() : 0D;
        ANIM_TIME_REF.setMemoized(animTimeSupplier);

        var boneAnimations = animation.boneAnimations();
        this.cursors = prepareKeyframeCursors(animation);
        this.easingOverride = animationController.animationProperties().easingType();
        var queues = boneAnimationQueueCache.resolveQueues(animation);

        for (var boneIndex = 0; boneIndex < boneAnimations.length; boneIndex++) {
            var boneAnimation = boneAnimations[boneIndex];
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

            var queue = queues[boneIndex];
            var snapshot = boneSnapshotCache.getOrNull(boneAnimation.boneName());

            if (snapshot == null || queue == null) {
                continue;
            }

            var rotationKeyframes = boneAnimation.rotationKeyframes();
            var positionKeyframes = boneAnimation.positionKeyframes();
            var scaleKeyframes = boneAnimation.scaleKeyframes();

            if (!rotationKeyframes.xKeyframes().isEmpty()) {
                var cursor = cursorIndex(boneIndex, ROTATION, 0);
                var x = target(rotationKeyframes.xChannel(), ROTATION, Axis.X, cursor);
                var y = target(rotationKeyframes.yChannel(), ROTATION, Axis.Y, cursor + 1);
                var z = target(rotationKeyframes.zChannel(), ROTATION, Axis.Z, cursor + 2);

                queue.addNextRotation(
                    null,
                    adjustedTick,
                    transitionLength,
                    snapshot,
                    bone.getInitialAzSnapshot(),
                    x,
                    y,
                    z
                );
            }

            // Position transitions even without keyframes (towards 0), as it always has.
            var positionCursor = cursorIndex(boneIndex, POSITION, 0);
            var posX = target(positionKeyframes.xChannel(), POSITION, Axis.X, positionCursor);
            var posY = target(positionKeyframes.yChannel(), POSITION, Axis.Y, positionCursor + 1);
            var posZ = target(positionKeyframes.zChannel(), POSITION, Axis.Z, positionCursor + 2);

            queue.addNextPosition(null, adjustedTick, transitionLength, snapshot, posX, posY, posZ);

            if (!scaleKeyframes.xKeyframes().isEmpty()) {
                var cursor = cursorIndex(boneIndex, SCALE, 0);
                var x = target(scaleKeyframes.xChannel(), SCALE, Axis.X, cursor);
                var y = target(scaleKeyframes.yChannel(), SCALE, Axis.Y, cursor + 1);
                var z = target(scaleKeyframes.zChannel(), SCALE, Axis.Z, cursor + 2);

                queue.addNextScale(null, adjustedTick, transitionLength, snapshot, x, y, z);
            }
        }
    }

    /**
     * The value {@code channel} has at the transition's target tick.
     */
    private double target(AzKeyframeChannel channel, int transform, Axis axis, int cursorIndex) {
        return sampleValue(channel, transform, axis, currentAdjustedTick, cursors, cursorIndex, easingOverride);
    }
}
