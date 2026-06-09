package mod.azure.azurelib.common.animation.controller.keyframe;

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

    private AzKeyframeLocation<AzKeyframe<IValue>> scratchLocation =
        new AzKeyframeLocation<>(new AzKeyframe<>(0, () -> 0, () -> 0), 0);

    protected AzAbstractKeyframeExecutor() {}

    /**
     * Convert a {@link AzKeyframeLocation} to an {@link AzAnimationPoint}. Reuses a scratch {@link AzKeyframeLocation}
     * to avoid allocation; also reuses a scratch {@link AzAnimationPoint} written into {@code out}.
     */
    protected AzAnimationPoint getAnimationPointAtTick(
        List<AzKeyframe<IValue>> frames,
        double tick,
        boolean isRotation,
        Axis axis
    ) {
        if (frames.isEmpty()) {
            scratchLocation = new AzKeyframeLocation<>(new AzKeyframe<>(0, () -> 0, () -> 0), 0);
        } else {
            scratchLocation = getCurrentKeyframeLocation(frames, tick, scratchLocation);
        }
        var currentFrame = scratchLocation.keyframe();
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

        return new AzAnimationPoint(
            currentFrame,
            scratchLocation.startTick(),
            currentFrame.length(),
            startValue,
            endValue
        );
    }

    /**
     * Returns the {@link AzKeyframe} relevant to the current tick time using binary search on precomputed cumulative
     * end-times. Falls back to linear scan if cumulative times are unavailable. Writes result into {@code scratch} to
     * avoid allocation.
     */
    protected AzKeyframeLocation<AzKeyframe<IValue>> getCurrentKeyframeLocation(
        List<AzKeyframe<IValue>> frames,
        double ageInTicks,
        AzKeyframeLocation<AzKeyframe<IValue>> scratch
    ) {
        var n = frames.size();
        var cumulative = new double[n];
        var total = 0D;
        for (var i = 0; i < n; i++) {
            total += frames.get(i).length();
            cumulative[i] = total;
        }

        int lo = 0, hi = n - 1, result = n - 1;
        while (lo <= hi) {
            var mid = (lo + hi) >>> 1;
            if (cumulative[mid] > ageInTicks) {
                result = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        var frame = frames.get(result);
        var startTick = ageInTicks - (cumulative[result] - frame.length());
        scratchLocation.set(frame, startTick);
        return scratchLocation;
    }

    /**
     * Legacy overload retained for any subclass overrides. Delegates to the scratch-based version.
     */
    protected AzKeyframeLocation<AzKeyframe<IValue>> getCurrentKeyframeLocation(
        List<AzKeyframe<IValue>> frames,
        double ageInTicks
    ) {
        return getCurrentKeyframeLocation(frames, ageInTicks, scratchLocation);
    }
}
