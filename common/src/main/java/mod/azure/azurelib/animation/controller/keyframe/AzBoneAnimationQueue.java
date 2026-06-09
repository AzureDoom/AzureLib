/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.animation.controller.keyframe;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.model.AzBoneSnapshot;

/**
 * A bone pseudo-stack for bone animation positions, scales, and rotations.
 * <p>
 * Backed by a flat pool of nine pre-allocated {@link AzAnimationPoint} instances (one per axis per transform type)
 * written via {@link AzAnimationPoint#set} each frame. This eliminates the per-frame queue-node and record allocations
 * that were the primary source of GC pressure in the animation pipeline.
 * </p>
 */
public class AzBoneAnimationQueue {

    private final AzBone bone;

    private static final int ROT_X = 0, ROT_Y = 1, ROT_Z = 2;

    private static final int POS_X = 3, POS_Y = 4, POS_Z = 5;

    private static final int SCL_X = 6, SCL_Y = 7, SCL_Z = 8;

    private final AzAnimationPoint[] pool = new AzAnimationPoint[9];

    private final boolean[] present = new boolean[9];

    public AzBoneAnimationQueue(AzBone bone) {
        this.bone = bone;
        for (int i = 0; i < 9; i++)
            pool[i] = new AzAnimationPoint();
    }

    public AzBone bone() {
        return bone;
    }

    private void write(int slot, AzKeyframe<?> keyframe, double tick, double length, double start, double end) {
        pool[slot].set(keyframe, tick, length, start, end);
        present[slot] = true;
    }

    public void clearFrame() {
        java.util.Arrays.fill(present, false);
    }

    public AzAnimationPoint pollRotX() {
        if (!present[ROT_X])
            return null;
        present[ROT_X] = false;
        return pool[ROT_X];
    }

    public AzAnimationPoint pollRotY() {
        if (!present[ROT_Y])
            return null;
        present[ROT_Y] = false;
        return pool[ROT_Y];
    }

    public AzAnimationPoint pollRotZ() {
        if (!present[ROT_Z])
            return null;
        present[ROT_Z] = false;
        return pool[ROT_Z];
    }

    public AzAnimationPoint pollPosX() {
        if (!present[POS_X])
            return null;
        present[POS_X] = false;
        return pool[POS_X];
    }

    public AzAnimationPoint pollPosY() {
        if (!present[POS_Y])
            return null;
        present[POS_Y] = false;
        return pool[POS_Y];
    }

    public AzAnimationPoint pollPosZ() {
        if (!present[POS_Z])
            return null;
        present[POS_Z] = false;
        return pool[POS_Z];
    }

    public AzAnimationPoint pollSclX() {
        if (!present[SCL_X])
            return null;
        present[SCL_X] = false;
        return pool[SCL_X];
    }

    public AzAnimationPoint pollSclY() {
        if (!present[SCL_Y])
            return null;
        present[SCL_Y] = false;
        return pool[SCL_Y];
    }

    public AzAnimationPoint pollSclZ() {
        if (!present[SCL_Z])
            return null;
        present[SCL_Z] = false;
        return pool[SCL_Z];
    }

    public void addPosXPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(POS_X, k, t, l, s, e);
    }

    public void addPosYPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(POS_Y, k, t, l, s, e);
    }

    public void addPosZPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(POS_Z, k, t, l, s, e);
    }

    public void addScaleXPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(SCL_X, k, t, l, s, e);
    }

    public void addScaleYPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(SCL_Y, k, t, l, s, e);
    }

    public void addScaleZPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(SCL_Z, k, t, l, s, e);
    }

    public void addRotationXPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(ROT_X, k, t, l, s, e);
    }

    public void addRotationYPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(ROT_Y, k, t, l, s, e);
    }

    public void addRotationZPoint(AzKeyframe<?> k, double t, double l, double s, double e) {
        write(ROT_Z, k, t, l, s, e);
    }

    public void addPositions(AzAnimationPoint x, AzAnimationPoint y, AzAnimationPoint z) {
        write(POS_X, x.keyframe, x.currentTick, x.transitionLength, x.animationStartValue, x.animationEndValue);
        write(POS_Y, y.keyframe, y.currentTick, y.transitionLength, y.animationStartValue, y.animationEndValue);
        write(POS_Z, z.keyframe, z.currentTick, z.transitionLength, z.animationStartValue, z.animationEndValue);
    }

    public void addScales(AzAnimationPoint x, AzAnimationPoint y, AzAnimationPoint z) {
        write(SCL_X, x.keyframe, x.currentTick, x.transitionLength, x.animationStartValue, x.animationEndValue);
        write(SCL_Y, y.keyframe, y.currentTick, y.transitionLength, y.animationStartValue, y.animationEndValue);
        write(SCL_Z, z.keyframe, z.currentTick, z.transitionLength, z.animationStartValue, z.animationEndValue);
    }

    public void addRotations(AzAnimationPoint x, AzAnimationPoint y, AzAnimationPoint z) {
        write(ROT_X, x.keyframe, x.currentTick, x.transitionLength, x.animationStartValue, x.animationEndValue);
        write(ROT_Y, y.keyframe, y.currentTick, y.transitionLength, y.animationStartValue, y.animationEndValue);
        write(ROT_Z, z.keyframe, z.currentTick, z.transitionLength, z.animationStartValue, z.animationEndValue);
    }

    public void addNextPosition(
        AzKeyframe<?> keyframe,
        double tick,
        double length,
        AzBoneSnapshot startSnapshot,
        AzAnimationPoint nx,
        AzAnimationPoint ny,
        AzAnimationPoint nz
    ) {
        write(POS_X, keyframe, tick, length, startSnapshot.getOffsetX(), nx.animationStartValue);
        write(POS_Y, keyframe, tick, length, startSnapshot.getOffsetY(), ny.animationStartValue);
        write(POS_Z, keyframe, tick, length, startSnapshot.getOffsetZ(), nz.animationStartValue);
    }

    public void addNextScale(
        AzKeyframe<?> keyframe,
        double tick,
        double length,
        AzBoneSnapshot startSnapshot,
        AzAnimationPoint nx,
        AzAnimationPoint ny,
        AzAnimationPoint nz
    ) {
        write(SCL_X, keyframe, tick, length, startSnapshot.getScaleX(), nx.animationStartValue);
        write(SCL_Y, keyframe, tick, length, startSnapshot.getScaleY(), ny.animationStartValue);
        write(SCL_Z, keyframe, tick, length, startSnapshot.getScaleZ(), nz.animationStartValue);
    }

    public void addNextRotation(
        AzKeyframe<?> keyframe,
        double tick,
        double length,
        AzBoneSnapshot startSnapshot,
        AzBoneSnapshot initialSnapshot,
        AzAnimationPoint nx,
        AzAnimationPoint ny,
        AzAnimationPoint nz
    ) {
        if (startSnapshot == null) {
            AzureLib.LOGGER.warn("Warning: startSnapshot is null. Animation may not behave as expected.");
            return;
        }
        write(
            ROT_X,
            keyframe,
            tick,
            length,
            startSnapshot.getRotX() - initialSnapshot.getRotX(),
            nx.animationStartValue
        );
        write(
            ROT_Y,
            keyframe,
            tick,
            length,
            startSnapshot.getRotY() - initialSnapshot.getRotY(),
            ny.animationStartValue
        );
        write(
            ROT_Z,
            keyframe,
            tick,
            length,
            startSnapshot.getRotZ() - initialSnapshot.getRotZ(),
            nz.animationStartValue
        );
    }
}
