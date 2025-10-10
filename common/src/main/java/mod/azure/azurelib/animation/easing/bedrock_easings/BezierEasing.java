package mod.azure.azurelib.animation.easing.bedrock_easings;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.util.Mth;
import org.joml.Vector2d;

import java.util.List;

import mod.azure.azurelib.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.animation.easing.AzEasingType;
import mod.azure.azurelib.animation.easing.AzEasingUtil;
import mod.azure.azurelib.core.math.IValue;

/**
 * The BezierEasing class represents an abstract easing type that facilitates smooth transitions in animation using
 * cubic Bézier curves. This is implemented as part of the AzEasingType interface and provides utilities for generating
 * Bézier curves based on animation parameters.
 * <p>
 * Subclasses must implement the {@code isEasingBefore} method to define whether the easing curve applies before or
 * after a specific condition in the animation timeline.
 * <p>
 * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
 */
public abstract class BezierEasing implements AzEasingType {

    private static final double DEFAULT_RIGHT_TIME = 0.1;

    private static final double DEFAULT_LEFT_TIME = -0.1;

    private static final int CURVE_RESOLUTION = 200;

    private static final double TICKS_PER_SECOND = 20;

    /**
     * Builds and returns a Double2DoubleFunction that applies easing transformation.
     *
     * @param value the input parameter used for configuring the transformer
     * @return a Double2DoubleFunction that applies an easing transformation
     */
    @Override
    public Double2DoubleFunction buildTransformer(Double value) {
        return AzEasingUtil.easeIn(AzEasingUtil::linear);
    }

    /**
     * Applies a specified easing transformation to an animation point based on the provided easing value and lerping
     * value.
     *
     * @param animationPoint the point within the animation sequence, containing keyframe data and other parameters
     * @param easingValue    the easing configuration value influencing the animation behavior
     * @param lerpValue      the interpolation value for determining the current animation state
     * @return the eased result as a double, representing the updated animation state
     */
    @Override
    public double apply(AzAnimationPoint animationPoint, Double easingValue, double lerpValue) {
        List<? extends IValue> easingArgs = animationPoint.keyframe().easingArgs();
        if (easingArgs.isEmpty()) {
            return handleNoEasingArgs(animationPoint, easingValue, lerpValue);
        }

        boolean easingBefore = isEasingBefore();
        double rightValue = easingBefore ? 0 : easingArgs.get(0).get();
        double rightTime = easingBefore ? DEFAULT_RIGHT_TIME : easingArgs.get(1).get();
        double leftValue = easingBefore ? easingArgs.get(0).get() : 0;
        double leftTime = easingBefore ? easingArgs.get(1).get() : DEFAULT_LEFT_TIME;

        if (easingArgs.size() > 3) {
            rightValue = easingArgs.get(2).get();
            rightTime = easingArgs.get(3).get();
        }

        leftValue = Math.toRadians(leftValue);
        rightValue = Math.toRadians(rightValue);

        double normalizedTransitionDuration = animationPoint.transitionLength() / TICKS_PER_SECOND;
        double clampedRightTime = Math.min(normalizedTransitionDuration, Math.max(rightTime, 0));
        double clampedLeftTime = Math.min(0, Math.max(leftTime, -normalizedTransitionDuration));

        CubicBezierCurve curve = buildBezierCurve(
            animationPoint,
            clampedLeftTime,
            clampedRightTime,
            leftValue,
            rightValue,
            normalizedTransitionDuration
        );
        double time = normalizedTransitionDuration * lerpValue;

        List<Vector2d> points = curve.getPoints(CURVE_RESOLUTION);
        Vector2d[] closestPoints = findClosestPoints(points, time);

        return Mth.lerp(
            Math.min(1, Math.max(Mth.lerp(time, closestPoints[0].x, closestPoints[1].x), 0)),
            closestPoints[0].y,
            closestPoints[1].y
        );
    }

    /**
     * Determines whether the easing process should occur before a specified condition or point in the animation
     * sequence. This method is abstract and must be implemented by subclasses to define the specific behavior of the
     * easing evaluation.
     *
     * @return true if the easing is configured to occur before the specified condition or animation evaluation point;
     *         false otherwise.
     */
    public abstract boolean isEasingBefore();

    /**
     * Handles the scenario where no specific easing arguments are provided by applying a linear interpolation between
     * the animation start value and end value, transformed through a calculated easing function.
     *
     * @param animationPoint the current animation point containing keyframe data and start and end values for the
     *                       animation
     * @param easingValue    the easing configuration value used to generate the transformation function
     * @param lerpValue      the interpolation (lerp) value to determine the current progress of the animation
     * @return the transformed interpolated value as a double, representing the current state of the animation
     */
    private double handleNoEasingArgs(AzAnimationPoint animationPoint, Double easingValue, double lerpValue) {
        Double2DoubleFunction transformer = buildTransformer(easingValue);
        return Mth.lerp(
            transformer.apply(lerpValue),
            animationPoint.animationStartValue(),
            animationPoint.animationEndValue()
        );
    }

    /**
     * Constructs a cubic Bézier curve using the provided animation point, time constraints, value adjustments, and
     * transition duration. The Bézier curve is defined by four control points calculated based on the input parameters.
     *
     * @param animationPoint               the animation point containing keyframe data and start/end animation values
     * @param clampedLeftTime              the time constraint for the left control point of the Bézier curve
     * @param clampedRightTime             the time constraint for the right control point of the Bézier curve
     * @param leftValue                    the adjustment value for the left control point relative to the starting
     *                                     point
     * @param rightValue                   the adjustment value for the right control point relative to the starting
     *                                     point
     * @param normalizedTransitionDuration the normalized duration of the transition for the curve's end points
     * @return a {@code CubicBezierCurve} instance representing the calculated curve
     */
    private CubicBezierCurve buildBezierCurve(
        AzAnimationPoint animationPoint,
        double clampedLeftTime,
        double clampedRightTime,
        double leftValue,
        double rightValue,
        double normalizedTransitionDuration
    ) {
        return new CubicBezierCurve(
            new Vector2d(0, animationPoint.animationStartValue()),
            new Vector2d(clampedRightTime, animationPoint.animationStartValue() + rightValue),
            new Vector2d(
                clampedLeftTime + normalizedTransitionDuration,
                animationPoint.animationEndValue() + leftValue
            ),
            new Vector2d(normalizedTransitionDuration, animationPoint.animationEndValue())
        );
    }

    /**
     * Finds the two closest points in a list of 2D points to a specified value of time. The distances between the
     * x-coordinates of the points and the given time are used to determine proximity.
     *
     * @param points the list of 2D points to search through
     * @param time   the target time value used to measure distance from each point's x-coordinate
     * @return an array containing the two closest points to the specified time, with the closest point at index 0 and
     *         the second closest point at index 1
     */
    private Vector2d[] findClosestPoints(List<Vector2d> points, double time) {
        Vector2d closest = new Vector2d();
        Vector2d secondClosest = new Vector2d();
        double closestDiff = Double.POSITIVE_INFINITY;
        double secondClosestDiff = Double.POSITIVE_INFINITY;

        for (Vector2d point : points) {
            double diff = Math.abs(point.x - time);
            if (diff < closestDiff) {
                secondClosest.set(closest);
                secondClosestDiff = closestDiff;

                closest.set(point);
                closestDiff = diff;
            } else if (diff < secondClosestDiff) {
                secondClosest.set(point);
                secondClosestDiff = diff;
            }
        }
        return new Vector2d[] { closest, secondClosest };
    }

}
