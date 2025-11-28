package mod.azure.azurelib.animation.controller.keyframe;

import java.util.ArrayDeque;
import java.util.List;

import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.object.Axis;

/**
 * AzAbstractKeyframeExecutor is a base class designed to handle animations and transitions between keyframes in a
 * generic and reusable fashion. It provides the foundational logic for determining the current state of an animation
 * based on the tick time and computing the animation's required values.
 */
public class AzAbstractKeyframeExecutor {

    private static final int MAX_POOL_SIZE = 512;

    private static final ArrayDeque<AzAnimationPoint> pointPool = new ArrayDeque<>(MAX_POOL_SIZE);

    protected AzAbstractKeyframeExecutor() {}

    /**
     * Convert a {@link AzKeyframeLocation} to an {@link AzAnimationPoint}
     */
    protected AzAnimationPoint getAnimationPointAtTick(
        List<AzKeyframe<IValue>> frames,
        double tick,
        boolean isRotation,
        Axis axis
    ) {
        if (frames.isEmpty()) {
            return obtainPoint(null, 0, 0, 0, 0);
        }

        AzKeyframeLocation<AzKeyframe<IValue>> location = getCurrentKeyframeLocation(frames, tick);
        var currentFrame = location.keyframe();
        var startValue = currentFrame.startValue().get();
        var endValue = currentFrame.endValue().get();

        if (isRotation) {
            if (!(currentFrame.startValue() instanceof Constant)) {
                startValue = Math.toRadians(startValue);
                if (axis == Axis.X || axis == Axis.Y)
                    startValue *= -1;
            }
            if (!(currentFrame.endValue() instanceof Constant)) {
                endValue = Math.toRadians(endValue);
                if (axis == Axis.X || axis == Axis.Y)
                    endValue *= -1;
            }
        }

        return obtainPoint(currentFrame, location.startTick(), currentFrame.length(), startValue, endValue);
    }

    /**
     * Returns the {@link AzKeyframe} relevant to the current tick time
     *
     * @param frames     The list of {@code Keyframes} to filter through
     * @param ageInTicks The current tick time
     * @return A new {@code KeyframeLocation} containing the current {@code Keyframe} and the tick time used to find it
     */
    protected AzKeyframeLocation<AzKeyframe<IValue>> getCurrentKeyframeLocation(
        List<AzKeyframe<IValue>> frames,
        double ageInTicks
    ) {
        var totalFrameTime = 0.0;

        for (var frame : frames) {
            totalFrameTime += frame.length();

            if (totalFrameTime > ageInTicks) {
                return new AzKeyframeLocation<>(frame, (ageInTicks - (totalFrameTime - frame.length())));
            }
        }

        return new AzKeyframeLocation<>(frames.get(frames.size() - 1), ageInTicks);
    }

    protected static AzAnimationPoint obtainPoint(
        AzKeyframe<?> keyframe,
        double currentTick,
        double len,
        double start,
        double end
    ) {
        AzAnimationPoint p = pointPool.pollFirst();
        if (p == null) {
            return new AzAnimationPoint(keyframe, currentTick, len, start, end);
        }
        p.reinit(keyframe, currentTick, len, start, end);
        return p;
    }

    protected static void recyclePoint(AzAnimationPoint point) {
        if (point == null)
            return;
        if (pointPool.size() < MAX_POOL_SIZE) {
            point.reset();
            pointPool.addFirst(point);
        }
    }
}
