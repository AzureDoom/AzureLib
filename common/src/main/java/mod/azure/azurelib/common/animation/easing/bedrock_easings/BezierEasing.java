package mod.azure.azurelib.common.animation.easing.bedrock_easings;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import net.minecraft.util.Mth;
import org.joml.Vector2d;

import java.util.List;

import mod.azure.azurelib.common.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.common.animation.easing.AzEasingType;
import mod.azure.azurelib.common.animation.easing.AzEasingUtil;
import mod.azure.azurelib.core.math.IValue;

/**
 * The BezierEasing class represents an abstract easing type that facilitates smooth transitions in animation using
 * cubic Bézier curves. This is implemented as part of the AzEasingType interface and provides utilities for generating
 * Bézier curves based on animation parameters.
 * <p>
 * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
 */
public abstract class BezierEasing implements AzEasingType {

    private static final double DEFAULT_RIGHT_TIME = 0.1;

    private static final double DEFAULT_LEFT_TIME = -0.1;

    private static final double TICKS_PER_SECOND = 20;

    private final Vector2d scratchA = new Vector2d();

    private final Vector2d scratchB = new Vector2d();

    private final Vector2d cpStart = new Vector2d();

    private final Vector2d cpControl1 = new Vector2d();

    private final Vector2d cpControl2 = new Vector2d();

    private final Vector2d cpEnd = new Vector2d();

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
        double rightValue = easingBefore ? 0 : easingArgs.getFirst().get();
        double rightTime = easingBefore ? DEFAULT_RIGHT_TIME : easingArgs.get(1).get();
        double leftValue = easingBefore ? easingArgs.getFirst().get() : 0;
        double leftTime = easingBefore ? easingArgs.get(1).get() : DEFAULT_LEFT_TIME;

        if (easingArgs.size() > 3) {
            rightValue = easingArgs.get(2).get();
            rightTime = easingArgs.get(3).get();
        }

        leftValue = Math.toRadians(leftValue);
        rightValue = Math.toRadians(rightValue);

        double normalizedTransitionDuration = animationPoint.transitionLength() / TICKS_PER_SECOND;
        double clampedRightTime = Math.clamp(rightTime, 0, normalizedTransitionDuration);
        double clampedLeftTime = Math.clamp(leftTime, -normalizedTransitionDuration, 0);

        CubicBezierCurve curve = buildBezierCurve(
            animationPoint,
            clampedLeftTime,
            clampedRightTime,
            leftValue,
            rightValue,
            normalizedTransitionDuration
        );

        double time = normalizedTransitionDuration * lerpValue;

        return curve.evaluateAtTime(time, scratchA, scratchB);
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
     * Builds a {@link CubicBezierCurve} using reusable control-point {@link Vector2d} instances. The returned
     * {@code CubicBezierCurve} is a record holding references to these fields — it must not be stored beyond the
     * current {@link #apply} call.
     */
    private CubicBezierCurve buildBezierCurve(
        AzAnimationPoint animationPoint,
        double clampedLeftTime,
        double clampedRightTime,
        double leftValue,
        double rightValue,
        double normalizedTransitionDuration
    ) {
        cpStart.set(0, animationPoint.animationStartValue());
        cpControl1.set(clampedRightTime, animationPoint.animationStartValue() + rightValue);
        cpControl2.set(clampedLeftTime + normalizedTransitionDuration, animationPoint.animationEndValue() + leftValue);
        cpEnd.set(normalizedTransitionDuration, animationPoint.animationEndValue());

        return new CubicBezierCurve(cpStart, cpControl1, cpControl2, cpEnd);
    }
}
