package mod.azure.azurelib.animation.controller.keyframe;

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
        AzKeyframeLocation<AzKeyframe<IValue>> location = frames.isEmpty()
            ? new AzKeyframeLocation<>(new AzKeyframe<>(0, () -> 0, () -> 0), 0)
            : getCurrentKeyframeLocation(frames, tick);
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

        return new AzAnimationPoint(currentFrame, location.startTick(), currentFrame.length(), startValue, endValue);
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
}
