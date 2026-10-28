package mod.azure.azurelib.animation.controller.keyframe;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.animation.controller.AzBoneSnapshotCache;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.primitive.AzBakedAnimation;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.animation.property.AzAnimationProperties;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.molang.MolangVariableRef;
import mod.azure.azurelib.core.object.Axis;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;
import mod.azure.azurelib.util.math.Mth;

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
        AzQueuedAnimation currentAnimation = animationController.currentAnimation();
        float transitionLength = animationController.animationProperties().transitionLength();
        AzAnimationProperties properties = animationController.effectiveProperties();
        adjustedTick = Math.min(adjustedTick, transitionLength); // Cap tick length

        AzBakedAnimation animation = currentAnimation.animation();
        double length = animation.length();
        // Blend towards the pose playback will actually start on: the start offset, from whichever end it plays from.
        double startProgress = Mth.clamp(properties.startTickOffset(), 0D, length);

        currentAdjustedTick = animationController.isPlayingReversed() ? length - startProgress : startProgress;
        ANIM_TIME_REF.setMemoized(animTimeSupplier);

        AzBoneAnimation[] boneAnimations = animation.boneAnimations();
        this.cursors = prepareKeyframeCursors(animation);
        this.easingOverride = properties.easingType();
        AzBoneAnimationQueue[] queues = boneAnimationQueueCache.resolveQueues(animation);

        for (int boneIndex = 0; boneIndex < boneAnimations.length; boneIndex++) {
            AzBoneAnimation boneAnimation = boneAnimations[boneIndex];
            AzBone bone = bones.get(boneAnimation.boneName());

            if (bone == null) {
                if (crashWhenCantFindBone)
                    throw new NoSuchElementException("Could not find bone: " + boneAnimation.boneName());

                continue;
            }

            // Bones outside the controller's mask are skipped before their keyframes are evaluated.
            if (!animationController.boneMask().includes(bone)) {
                continue;
            }

            AzBoneAnimationQueue queue = queues[boneIndex];
            AzBoneSnapshot snapshot = boneSnapshotCache.getOrNull(boneAnimation.boneName());

            if (snapshot == null || queue == null) {
                continue;
            }

            // Molang's `this` in the target keyframes reads this bone's channels.
            this.channelBone = bone;

            AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes = boneAnimation.rotationKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes = boneAnimation.positionKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes = boneAnimation.scaleKeyframes();

            if (!rotationKeyframes.xKeyframes().isEmpty()) {
                int cursor = cursorIndex(boneIndex, ROTATION, 0);
                double x = target(rotationKeyframes.xChannel(), ROTATION, Axis.X, cursor);
                double y = target(rotationKeyframes.yChannel(), ROTATION, Axis.Y, cursor + 1);
                double z = target(rotationKeyframes.zChannel(), ROTATION, Axis.Z, cursor + 2);

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
            int positionCursor = cursorIndex(boneIndex, POSITION, 0);
            double posX = target(positionKeyframes.xChannel(), POSITION, Axis.X, positionCursor);
            double posY = target(positionKeyframes.yChannel(), POSITION, Axis.Y, positionCursor + 1);
            double posZ = target(positionKeyframes.zChannel(), POSITION, Axis.Z, positionCursor + 2);

            queue.addNextPosition(null, adjustedTick, transitionLength, snapshot, posX, posY, posZ);

            if (!scaleKeyframes.xKeyframes().isEmpty()) {
                int cursor = cursorIndex(boneIndex, SCALE, 0);
                double x = target(scaleKeyframes.xChannel(), SCALE, Axis.X, cursor);
                double y = target(scaleKeyframes.yChannel(), SCALE, Axis.Y, cursor + 1);
                double z = target(scaleKeyframes.zChannel(), SCALE, Axis.Z, cursor + 2);

                queue.addNextScale(null, adjustedTick, transitionLength, snapshot, x, y, z);
            }
        }

        this.channelBone = null;
    }

    /**
     * The value {@code channel} has at the transition's target tick.
     */
    private double target(AzKeyframeChannel channel, int transform, Axis axis, int cursorIndex) {
        return sampleValue(channel, transform, axis, currentAdjustedTick, cursors, cursorIndex, easingOverride);
    }
}
