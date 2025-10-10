/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import java.util.LinkedList;
import java.util.Queue;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.controller.AzAnimationController;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * A bone pseudo-stack for bone animation positions, scales, and rotations. Animation points are calculated then pushed
 * onto their respective queues to be used for transformations in rendering
 */
public record AzBoneAnimationQueue(
    AzBone bone,
    Queue<AzAnimationPoint> rotationXQueue,
    Queue<AzAnimationPoint> rotationYQueue,
    Queue<AzAnimationPoint> rotationZQueue,
    Queue<AzAnimationPoint> positionXQueue,
    Queue<AzAnimationPoint> positionYQueue,
    Queue<AzAnimationPoint> positionZQueue,
    Queue<AzAnimationPoint> scaleXQueue,
    Queue<AzAnimationPoint> scaleYQueue,
    Queue<AzAnimationPoint> scaleZQueue
) {

    public AzBoneAnimationQueue(AzBone bone) {
        // TODO: Optimize
        this(
            bone,
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>(),
            new LinkedList<>()
        );
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#positionXQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addPosXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#positionYQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addPosYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#positionZQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addPosZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new X, Y, and Z position {@link AzAnimationPoint} to their respective queues
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (base on the {@link AzAnimationController}
     * @param startSnapshot    The {@link AzBoneSnapshot} that serves as the starting positions relevant to the keyframe
     *                         provided
     * @param nextXPoint       The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextYPoint       The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextZPoint       The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     */
    public void addNextPosition(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        AzBoneSnapshot startSnapshot,
        AzAnimationPoint nextXPoint,
        AzAnimationPoint nextYPoint,
        AzAnimationPoint nextZPoint
    ) {
        addPosXPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getOffsetX(),
            nextXPoint.animationStartValue()
        );
        addPosYPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getOffsetY(),
            nextYPoint.animationStartValue()
        );
        addPosZPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getOffsetZ(),
            nextZPoint.animationStartValue()
        );
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#scaleXQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addScaleXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#scaleYQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addScaleYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#scaleZQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addScaleZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new X, Y, and Z scale {@link AzAnimationPoint} to their respective queues
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (base on the {@link AzAnimationController}
     * @param startSnapshot    The {@link AzBoneSnapshot} that serves as the starting scales relevant to the keyframe
     *                         provided
     * @param nextXPoint       The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextYPoint       The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextZPoint       The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     */
    public void addNextScale(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        AzBoneSnapshot startSnapshot,
        AzAnimationPoint nextXPoint,
        AzAnimationPoint nextYPoint,
        AzAnimationPoint nextZPoint
    ) {
        addScaleXPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getScaleX(),
            nextXPoint.animationStartValue()
        );
        addScaleYPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getScaleY(),
            nextYPoint.animationStartValue()
        );
        addScaleZPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getScaleZ(),
            nextZPoint.animationStartValue()
        );
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#rotationXQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addRotationXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#rotationYQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addRotationYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new {@link AzAnimationPoint} to the {@link AzBoneAnimationQueue#rotationZQueue}
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (based on the {@link AzAnimationController})
     * @param startValue       The value of the point at the start of its transition
     * @param endValue         The value of the point at the end of its transition
     */
    public void addRotationZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    /**
     * Add a new X, Y, and Z scale {@link AzAnimationPoint} to their respective queues
     *
     * @param keyframe         The {@code Nullable} Keyframe relevant to the animation point
     * @param lerpedTick       The lerped time (current tick + partial tick) that the point starts at
     * @param transitionLength The length of the transition (base on the {@link AzAnimationController}
     * @param startSnapshot    The {@link AzBoneSnapshot} that serves as the starting rotations relevant to the keyframe
     *                         provided
     * @param initialSnapshot  The {@link AzBoneSnapshot} that serves as the unmodified rotations of the bone
     * @param nextXPoint       The X {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextYPoint       The Y {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     * @param nextZPoint       The Z {@code AnimationPoint} that is next in the queue, to serve as the end value of the
     *                         new point
     */
    public void addNextRotation(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        AzBoneSnapshot startSnapshot,
        AzBoneSnapshot initialSnapshot,
        AzAnimationPoint nextXPoint,
        AzAnimationPoint nextYPoint,
        AzAnimationPoint nextZPoint
    ) {
        if (startSnapshot == null) {
            AzureLib.LOGGER.warn("Warning: startSnapshot is null. Animation may not behave as expected.");
            return;
        }
        addRotationXPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getRotX() - initialSnapshot.getRotX(),
            nextXPoint.animationStartValue()
        );
        addRotationYPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getRotY() - initialSnapshot.getRotY(),
            nextYPoint.animationStartValue()
        );
        addRotationZPoint(
            keyframe,
            lerpedTick,
            transitionLength,
            startSnapshot.getRotZ() - initialSnapshot.getRotZ(),
            nextZPoint.animationStartValue()
        );
    }

    /**
     * Add an X, Y, and Z position {@link AzAnimationPoint} to their respective queues
     *
     * @param xPoint The x position {@code AnimationPoint} to add
     * @param yPoint The y position {@code AnimationPoint} to add
     * @param zPoint The z position {@code AnimationPoint} to add
     */
    public void addPositions(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.positionXQueue.add(xPoint);
        this.positionYQueue.add(yPoint);
        this.positionZQueue.add(zPoint);
    }

    /**
     * Add an X, Y, and Z scale {@link AzAnimationPoint} to their respective queues
     *
     * @param xPoint The x scale {@code AnimationPoint} to add
     * @param yPoint The y scale {@code AnimationPoint} to add
     * @param zPoint The z scale {@code AnimationPoint} to add
     */
    public void addScales(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.scaleXQueue.add(xPoint);
        this.scaleYQueue.add(yPoint);
        this.scaleZQueue.add(zPoint);
    }

    /**
     * Add an X, Y, and Z rotation {@link AzAnimationPoint} to their respective queues
     *
     * @param xPoint The x rotation {@code AnimationPoint} to add
     * @param yPoint The y rotation {@code AnimationPoint} to add
     * @param zPoint The z rotation {@code AnimationPoint} to add
     */
    public void addRotations(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.rotationXQueue.add(xPoint);
        this.rotationYQueue.add(yPoint);
        this.rotationZQueue.add(zPoint);
    }
}
