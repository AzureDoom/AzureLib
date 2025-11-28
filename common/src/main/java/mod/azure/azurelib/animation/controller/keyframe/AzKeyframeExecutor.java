package mod.azure.azurelib.animation.controller.keyframe;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.DoubleSupplier;

import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.animation.controller.AzBoneAnimationQueueCache;
import mod.azure.azurelib.animation.primitive.AzQueuedAnimation;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangParser;
import mod.azure.azurelib.core.molang.MolangQueries;
import mod.azure.azurelib.core.object.Axis;

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

    private double currentAnimTimeSeconds;

    private final DoubleSupplier animTimeSupplier = () -> currentAnimTimeSeconds;

    private final Map<String, BoneCache> boneCache = new HashMap<>();

    protected static final AzAnimationPoint EMPTY_POINT = new AzAnimationPoint(null, 0, 0, 0, 0);

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
    public void execute(@NotNull AzQueuedAnimation currentAnimation, T animatable, boolean crashWhenCantFindBone) {
        var keyframeCallbackHandler = animationController.keyframeManager().keyframeCallbackHandler();
        var controllerTimer = animationController.controllerTimer();

        final double adjustedTick = controllerTimer.getAdjustedTick();
        this.currentAnimTimeSeconds = adjustedTick / 20d;

        MolangParser.INSTANCE.setMemoizedValue(MolangQueries.ANIM_TIME, animTimeSupplier);

        for (var boneAnimation : currentAnimation.animation().boneAnimations()) {
            var boneName = boneAnimation.boneName();
            var boneQueue = boneAnimationQueueCache.getOrNull(boneName);
            if (boneQueue == null) {
                if (crashWhenCantFindBone) {
                    throw new NoSuchElementException("Could not find bone: " + boneName);
                }
                continue;
            }

            var cache = boneCache.computeIfAbsent(boneName, n -> new BoneCache());
            if (cache.lastTick == adjustedTick)
                continue; // already updated this tick
            cache.lastTick = adjustedTick;

            var rot = boneAnimation.rotationKeyframes();
            var pos = boneAnimation.positionKeyframes();
            var scl = boneAnimation.scaleKeyframes();

            if (stackIsNotEmpty(rot))
                updateRotation(rot, boneQueue, adjustedTick, cache);
            if (stackIsNotEmpty(pos))
                updatePosition(pos, boneQueue, adjustedTick, cache);
            if (stackIsNotEmpty(scl))
                updateScale(scl, boneQueue, adjustedTick, cache);
        }

        keyframeCallbackHandler.handle(animatable, adjustedTick);
    }

    private boolean stackIsNotEmpty(AzKeyframeStack<?> stack) {
        return !stack.xKeyframes().isEmpty() || !stack.yKeyframes().isEmpty() || !stack.zKeyframes().isEmpty();
    }

    private void updateRotation(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double tick,
        BoneCache cache
    ) {
        AzAnimationPoint newX = getAnimationPointAtTick(keyframes.xKeyframes(), tick, true, Axis.X);
        AzAnimationPoint newY = getAnimationPointAtTick(keyframes.yKeyframes(), tick, true, Axis.Y);
        AzAnimationPoint newZ = getAnimationPointAtTick(keyframes.zKeyframes(), tick, true, Axis.Z);

        if (cache.rotX != EMPTY_POINT && cache.rotX != newX)
            recyclePoint(cache.rotX);
        if (cache.rotY != EMPTY_POINT && cache.rotY != newY)
            recyclePoint(cache.rotY);
        if (cache.rotZ != EMPTY_POINT && cache.rotZ != newZ)
            recyclePoint(cache.rotZ);

        cache.rotX = getOrDefault(newX, cache.rotX);
        cache.rotY = getOrDefault(newY, cache.rotY);
        cache.rotZ = getOrDefault(newZ, cache.rotZ);

        queue.addRotations(cache.rotX, cache.rotY, cache.rotZ);
    }

    private void updatePosition(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double tick,
        BoneCache cache
    ) {
        AzAnimationPoint newX = getAnimationPointAtTick(keyframes.xKeyframes(), tick, false, Axis.X);
        AzAnimationPoint newY = getAnimationPointAtTick(keyframes.yKeyframes(), tick, false, Axis.Y);
        AzAnimationPoint newZ = getAnimationPointAtTick(keyframes.zKeyframes(), tick, false, Axis.Z);

        if (cache.posX != EMPTY_POINT && cache.posX != newX)
            recyclePoint(cache.posX);
        if (cache.posY != EMPTY_POINT && cache.posY != newY)
            recyclePoint(cache.posY);
        if (cache.posZ != EMPTY_POINT && cache.posZ != newZ)
            recyclePoint(cache.posZ);

        cache.posX = getOrDefault(newX, cache.posX);
        cache.posY = getOrDefault(newY, cache.posY);
        cache.posZ = getOrDefault(newZ, cache.posZ);

        queue.addPositions(cache.posX, cache.posY, cache.posZ);
    }

    private void updateScale(
        AzKeyframeStack<AzKeyframe<IValue>> keyframes,
        AzBoneAnimationQueue queue,
        double tick,
        BoneCache cache
    ) {
        AzAnimationPoint newX = getAnimationPointAtTick(keyframes.xKeyframes(), tick, false, Axis.X);
        AzAnimationPoint newY = getAnimationPointAtTick(keyframes.yKeyframes(), tick, false, Axis.Y);
        AzAnimationPoint newZ = getAnimationPointAtTick(keyframes.zKeyframes(), tick, false, Axis.Z);

        if (cache.sclX != EMPTY_POINT && cache.sclX != newX)
            recyclePoint(cache.sclX);
        if (cache.sclY != EMPTY_POINT && cache.sclY != newY)
            recyclePoint(cache.sclY);
        if (cache.sclZ != EMPTY_POINT && cache.sclZ != newZ)
            recyclePoint(cache.sclZ);

        cache.sclX = getOrDefault(newX, cache.sclX);
        cache.sclY = getOrDefault(newY, cache.sclY);
        cache.sclZ = getOrDefault(newZ, cache.sclZ);

        queue.addScales(cache.sclX, cache.sclY, cache.sclZ);
    }

    private static AzAnimationPoint getOrDefault(AzAnimationPoint value, AzAnimationPoint fallback) {
        return value != null ? value : fallback;
    }

    private static class BoneCache {

        double lastTick = -1;

        AzAnimationPoint rotX = EMPTY_POINT, rotY = EMPTY_POINT, rotZ = EMPTY_POINT;

        AzAnimationPoint posX = EMPTY_POINT, posY = EMPTY_POINT, posZ = EMPTY_POINT;

        AzAnimationPoint sclX = EMPTY_POINT, sclY = EMPTY_POINT, sclZ = EMPTY_POINT;
    }
}
