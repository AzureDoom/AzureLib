package mod.azure.azurelib.animation.easing.bedrock_easings;

import java.util.ArrayList;
import java.util.List;

public class CubicBezierCurve {

    private final Vector2d startPoint;
    private final Vector2d controlPoint1;
    private final Vector2d controlPoint2;
    private final Vector2d endPoint;

    /**
     * A constant representing the weight of the control points in a cubic Bézier curve calculation. Used to multiply
     * the influence of the first and second control points when computing the curve. This value ensures proper
     * weighting in the mathematical representation of the curve.
     */
    private static final int BEZIER_WEIGHT = 3;

    /**
     * Constructor for creating a cubic Bézier curve.
     *
     * @param startPoint    The starting point of the curve.
     * @param controlPoint1 The first control point of the curve.
     * @param controlPoint2 The second control point of the curve.
     * @param endPoint      The ending point of the curve.
     */
    public CubicBezierCurve(Vector2d startPoint, Vector2d controlPoint1, Vector2d controlPoint2, Vector2d endPoint) {
        this.startPoint = startPoint;
        this.controlPoint1 = controlPoint1;
        this.controlPoint2 = controlPoint2;
        this.endPoint = endPoint;
    }

    /**
     * Computes a point on the Bézier curve for a given parameter t.
     *
     * @param progress The parameter must be between 0 and 1.
     * @return The computed point on the curve.
     */
    public Vector2d getPoint(float progress) {
        // TODO: look at maybe returning null instead isntancing a new Vector2D. Requires testing.
        return getPoint(progress, new Vector2d());
    }

    /**
     * Computes (or updates) a point on the curve with the given parameter.
     *
     * @param progress The parameter must be between 0 and 1.
     * @param target   An optional target Vector2d to store the result.
     * @return The computed point on the curve.
     * @throws IllegalArgumentException If t is outside the range [0, 1].
     */
    public Vector2d getPoint(float progress, Vector2d target) {
        if (progress < 0 || progress > 1) {
            throw new IllegalArgumentException("Parameter t must be in the range [0, 1].");
        }

        if (target == null) {
            target = new Vector2d();
        }

        float oneMinusProgress = 1 - progress;
        float progressSquared = progress * progress;
        float oneMinusProgressSquared = oneMinusProgress * oneMinusProgress;
        float oneMinusProgressCubed = oneMinusProgressSquared * oneMinusProgress;
        float progressCubed = progressSquared * progress;

        target.x = oneMinusProgressCubed * startPoint.x() + BEZIER_WEIGHT * oneMinusProgressSquared * progress
            * controlPoint1.x() + BEZIER_WEIGHT * oneMinusProgress * progressSquared
                * controlPoint2.x() + progressCubed * endPoint.x();
        target.y = oneMinusProgressCubed * startPoint.y() + BEZIER_WEIGHT * oneMinusProgressSquared * progress
            * controlPoint1.y() + BEZIER_WEIGHT * oneMinusProgress * progressSquared
                * controlPoint2.y() + progressCubed * endPoint.y();

        return target;
    }

    /**
     * Computes a series of points along the Bézier curve, divided evenly.
     *
     * @param divisions The number of divisions must be greater than 0.
     * @return A list of computed points.
     * @throws IllegalArgumentException If divisions are less than or equal to zero.
     */
    public List<Vector2d> getPoints(int divisions) {
        if (divisions <= 0) {
            throw new IllegalArgumentException("Divisions must be greater than 0.");
        }

        List<Vector2d> points = new ArrayList<>();

        for (int step = 0; step <= divisions; step++) {
            points.add(getPoint((float) step / divisions));
        }

        return points;
    }

    public Vector2d startPoint() {
        return startPoint;
    }

    public Vector2d controlPoint1() {
        return controlPoint1;
    }

    public Vector2d controlPoint2() {
        return controlPoint2;
    }

    public Vector2d endPoint() {
        return endPoint;
    }

}
