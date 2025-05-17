/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import java.util.LinkedList;
import java.util.Queue;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * A bone pseudo-stack for bone animation positions, scales, and rotations. Animation points are calculated then pushed
 * onto their respective queues to be used for transformations in rendering.
 */
public class AzBoneAnimationQueue {

    private final AzBone bone;

    private final Queue<AzAnimationPoint> rotationXQueue;

    private final Queue<AzAnimationPoint> rotationYQueue;

    private final Queue<AzAnimationPoint> rotationZQueue;

    private final Queue<AzAnimationPoint> positionXQueue;

    private final Queue<AzAnimationPoint> positionYQueue;

    private final Queue<AzAnimationPoint> positionZQueue;

    private final Queue<AzAnimationPoint> scaleXQueue;

    private final Queue<AzAnimationPoint> scaleYQueue;

    private final Queue<AzAnimationPoint> scaleZQueue;

    public AzBoneAnimationQueue(AzBone bone) {
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

    public AzBoneAnimationQueue(
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
        this.bone = bone;
        this.rotationXQueue = rotationXQueue;
        this.rotationYQueue = rotationYQueue;
        this.rotationZQueue = rotationZQueue;
        this.positionXQueue = positionXQueue;
        this.positionYQueue = positionYQueue;
        this.positionZQueue = positionZQueue;
        this.scaleXQueue = scaleXQueue;
        this.scaleYQueue = scaleYQueue;
        this.scaleZQueue = scaleZQueue;
    }

    public AzBone bone() {
        return bone;
    }

    public Queue<AzAnimationPoint> rotationXQueue() {
        return rotationXQueue;
    }

    public Queue<AzAnimationPoint> rotationYQueue() {
        return rotationYQueue;
    }

    public Queue<AzAnimationPoint> rotationZQueue() {
        return rotationZQueue;
    }

    public Queue<AzAnimationPoint> positionXQueue() {
        return positionXQueue;
    }

    public Queue<AzAnimationPoint> positionYQueue() {
        return positionYQueue;
    }

    public Queue<AzAnimationPoint> positionZQueue() {
        return positionZQueue;
    }

    public Queue<AzAnimationPoint> scaleXQueue() {
        return scaleXQueue;
    }

    public Queue<AzAnimationPoint> scaleYQueue() {
        return scaleYQueue;
    }

    public Queue<AzAnimationPoint> scaleZQueue() {
        return scaleZQueue;
    }

    public void addPosXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addPosYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addPosZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.positionZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

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

    public void addScaleXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addScaleYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addScaleZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.scaleZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

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

    public void addRotationXPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationXQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addRotationYPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationYQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

    public void addRotationZPoint(
        AzKeyframe<?> keyframe,
        double lerpedTick,
        double transitionLength,
        double startValue,
        double endValue
    ) {
        this.rotationZQueue.add(new AzAnimationPoint(keyframe, lerpedTick, transitionLength, startValue, endValue));
    }

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

    public void addPositions(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.positionXQueue.add(xPoint);
        this.positionYQueue.add(yPoint);
        this.positionZQueue.add(zPoint);
    }

    public void addScales(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.scaleXQueue.add(xPoint);
        this.scaleYQueue.add(yPoint);
        this.scaleZQueue.add(zPoint);
    }

    public void addRotations(AzAnimationPoint xPoint, AzAnimationPoint yPoint, AzAnimationPoint zPoint) {
        this.rotationXQueue.add(xPoint);
        this.rotationYQueue.add(yPoint);
        this.rotationZQueue.add(zPoint);
    }
}
