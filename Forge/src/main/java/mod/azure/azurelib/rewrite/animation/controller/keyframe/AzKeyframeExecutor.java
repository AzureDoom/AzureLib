package mod.azure.azurelib.rewrite.animation.controller.keyframe;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.object.Axis;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationController;
import mod.azure.azurelib.rewrite.animation.controller.AzAnimationControllerTimer;
import mod.azure.azurelib.rewrite.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.rewrite.animation.primitive.AzQueuedAnimation;

import java.util.NoSuchElementException;

/**
 * AzKeyframeExecutor is a specialized implementation of {@link AzAbstractKeyframeExecutor}, designed to handle
 * keyframe-based animations for animatable objects. It delegates animation control to an {@link AzAnimationController}
 * and manages bone animation queues through an {@link AzBoneAnimationQueueCache}. <br>
 * This class processes and applies transformations such as rotation, position, and scale to bone animations, based on
 * the current tick time and the keyframes associated with each bone animation.
 *
 * @param <T> The type of the animatable object to which the keyframe animations will be applied
 */
public class AzKeyframeExecutor<T> extends AzAbstractKeyframeExecutor {

    private final AzAnimationController<T> animationController;

    private final AzBoneAnimationQueueCache<T> boneAnimationQueueCache;

    public AzKeyframeExecutor(
        AzAnimationController<T> animationController,
        AzBoneAnimationQueueCache<T> boneAnimationQueueCache
    ) {
        this.animationController = animationController;
        this.boneAnimationQueueCache = boneAnimationQueueCache;
    }

    /**
     * Handle the current animation's state modifications and translations
     *
     * @param crashWhenCantFindBone Whether the controller should throw an exception when unable to find the required
     *                              bone, or continue with the remaining bones
     */
    public void execute(AzQueuedAnimation currentAnimation, T animatable, boolean crashWhenCantFindBone) {
        AzKeyframeCallbackHandler<T> keyframeCallbackHandler = animationController.keyframeManager()
            .keyframeCallbackHandler();
        AzAnimationControllerTimer<T> controllerTimer = animationController.controllerTimer();
        float transitionLength = animationController.animationProperties().transitionLength();

        final double finalAdjustedTick = controllerTimer.getAdjustedTick();

        MolangParser.INSTANCE.setMemoizedValue(MolangQueries.ANIM_TIME, () -> finalAdjustedTick / 20d);

        for (AzBoneAnimation boneAnimation : currentAnimation.animation().boneAnimations()) {
            AzBoneAnimationQueue boneAnimationQueue = boneAnimationQueueCache.getOrNull(boneAnimation.boneName());

            if (boneAnimationQueue == null) {
                if (crashWhenCantFindBone) {
                    throw new NoSuchElementException("Could not find bone: " + boneAnimation.boneName());
                }

                continue;
            }

            AzKeyframeStack<AzKeyframe<IValue>> rotationKeyframes = boneAnimation.rotationKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> positionKeyframes = boneAnimation.positionKeyframes();
            AzKeyframeStack<AzKeyframe<IValue>> scaleKeyframes = boneAnimation.scaleKeyframes();
            double adjustedTick = controllerTimer.getAdjustedTick();

            updateRotation(rotationKeyframes, boneAnimationQueue, adjustedTick);
            updatePosition(positionKeyframes, boneAnimationQueue, adjustedTick);
            updateScale(scaleKeyframes, boneAnimationQueue, adjustedTick);
        }

        // TODO: Is this correct???
        controllerTimer.addToAdjustedTick(transitionLength);

        keyframeCallbackHandler.handle(animatable, controllerTimer.getAdjustedTick());
    }

    private void updateRotation(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double adjustedTick
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), adjustedTick, true, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), adjustedTick, true, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), adjustedTick, true, Axis.Z);

        queue.addRotations(x, y, z);
    }

    private void updatePosition(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double adjustedTick
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), adjustedTick, false, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), adjustedTick, false, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), adjustedTick, false, Axis.Z);

        queue.addPositions(x, y, z);
    }

    private void updateScale(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double adjustedTick
    ) {
        if (keyframes.xKeyframes().isEmpty()) {
            return;
        }

        AzAnimationPoint x = getAnimationPointAtTick(keyframes.xKeyframes(), adjustedTick, false, Axis.X);
        AzAnimationPoint y = getAnimationPointAtTick(keyframes.yKeyframes(), adjustedTick, false, Axis.Y);
        AzAnimationPoint z = getAnimationPointAtTick(keyframes.zKeyframes(), adjustedTick, false, Axis.Z);

        queue.addScales(x, y, z);
    }
}
