package mod.azure.azurelib.animation.easing.bedrock_easings;

/**
 * Represents a cubic Bézier curve in 2D space, defined by four control points.
 * <p>
 * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
 *
 * @param startPoint    The starting point of the curve.
 * @param controlPoint1 The first control point of the curve.
 * @param controlPoint2 The second control point of the curve.
 * @param endPoint      The ending point of the curve.
 */
public record CubicBezierCurve(
    Vector2d startPoint,
    Vector2d controlPoint1,
    Vector2d controlPoint2,
    Vector2d endPoint
) {

    private static final int BEZIER_WEIGHT = 3;

    /**
     * Computes (or updates) a point on the curve for a given parameter {@code progress} in [0, 1]. Writes the result
     * into {@code target} to avoid allocation.
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

        target.x = oneMinusProgressCubed * startPoint.x()
            + BEZIER_WEIGHT * oneMinusProgressSquared * progress * controlPoint1.x()
            + BEZIER_WEIGHT * oneMinusProgress * progressSquared * controlPoint2.x()
            + progressCubed * endPoint.x();
        target.y = oneMinusProgressCubed * startPoint.y()
            + BEZIER_WEIGHT * oneMinusProgressSquared * progress * controlPoint1.y()
            + BEZIER_WEIGHT * oneMinusProgress * progressSquared * controlPoint2.y()
            + progressCubed * endPoint.y();

        return target;
    }

    /**
     * Evaluates the curve y-value at a given x (time) using binary search on the parameter {@code t}, exploiting the
     * fact that x is monotonically increasing for well-formed animation curves.
     * <p>
     * This replaces the previous O(resolution) approach of sampling 200+ points and scanning for the nearest two.
     * Binary search converges in ~20 iterations regardless of resolution, with zero heap allocation beyond the two
     * scratch {@link Vector2d} instances passed in.
     * </p>
     *
     * @param time     Target x value (time along the curve)
     * @param scratchA Reusable Vector2d scratch — caller owns this, it will be overwritten
     * @param scratchB Reusable Vector2d scratch — caller owns this, it will be overwritten
     * @return Interpolated y value at the given time
     */
    public double evaluateAtTime(double time, Vector2d scratchA, Vector2d scratchB) {
        float lo = 0f, hi = 1f;

        for (int i = 0; i < 20; i++) {
            float mid = (lo + hi) * 0.5f;
            getPoint(mid, scratchA);
            if (scratchA.x < time) {
                lo = mid;
            } else {
                hi = mid;
            }
        }

        getPoint(lo, scratchA);
        getPoint(hi, scratchB);

        double dx = scratchB.x - scratchA.x;
        if (Math.abs(dx) < 1e-10) {
            return scratchA.y;
        }

        double frac = (time - scratchA.x) / dx;
        return scratchA.y + frac * (scratchB.y - scratchA.y);
    }
}
