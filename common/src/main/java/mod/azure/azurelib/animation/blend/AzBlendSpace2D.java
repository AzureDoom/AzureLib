package mod.azure.azurelib.animation.blend;

import java.util.ArrayList;
import java.util.List;

import mod.azure.azurelib.animation.AzAnimator;

/**
 * A blend space over two inputs, such as forward and strafe speed, or aim yaw and pitch. Samples can be placed
 * anywhere; weights use gradient band interpolation (see {@link AzBlendWeights#gradientBand}), so the input landing on
 * a sample plays exactly that sample and moving between samples blends smoothly.
 *
 * <pre>{@code
 * AzBlendSpace2D.builder(this, "strafe")
 *     .input((entity, partialTick) -> entity.strafeSpeed(), (entity, partialTick) -> entity.forwardSpeed())
 *     .sample(0, 0, "idle")
 *     .sample(0, 1, "walk_forward")
 *     .sample(0, -1, "walk_backward")
 *     .sample(-1, 0, "walk_left")
 *     .sample(1, 0, "walk_right")
 *     .build()
 *     .addTo(container);
 * }</pre>
 *
 * @param <T> the animatable type
 */
public final class AzBlendSpace2D<T> extends AzBlendSpace<T> {

    private final double[] xs;

    private final double[] ys;

    private final AzBlendInput<T> inputX;

    private final AzBlendInput<T> inputY;

    private double manualX, manualY;

    private double smoothedX = Double.NaN, smoothedY = Double.NaN;

    private AzBlendSpace2D(Builder<T> builder) {
        super(builder.animator, builder.name, builder.settings, builder.points.stream().map(Point::animation).toList());
        this.xs = builder.points.stream().mapToDouble(Point::x).toArray();
        this.ys = builder.points.stream().mapToDouble(Point::y).toArray();
        this.inputX = builder.inputX;
        this.inputY = builder.inputY;
    }

    public static <T> Builder<T> builder(AzAnimator<?, T> animator, String name) {
        return new Builder<>(animator, name);
    }

    public void setInput(double x, double y) {
        this.manualX = x;
        this.manualY = y;
    }

    public double inputX() {
        return Double.isNaN(smoothedX) ? manualX : smoothedX;
    }

    public double inputY() {
        return Double.isNaN(smoothedY) ? manualY : smoothedY;
    }

    @Override
    protected void computeWeights(T animatable, float partialTick, double smoothing, double[] dest) {
        double rawX = inputX == null ? manualX : inputX.get(animatable, partialTick);
        double rawY = inputY == null ? manualY : inputY.get(animatable, partialTick);
        smoothedX = approach(smoothedX, rawX, smoothing);
        smoothedY = approach(smoothedY, rawY, smoothing);
        AzBlendWeights.gradientBand(xs, ys, smoothedX, smoothedY, dest);
    }

    private static final class Point {

        private final double x;

        private final double y;

        private final String animation;

        private Point(
            double x,
            double y,
            String animation
        ) {
            this.x = x;
            this.y = y;
            this.animation = animation;
        }

        public double x() {
            return x;
        }

        public double y() {
            return y;
        }

        public String animation() {
            return animation;
        }
    }

    public static final class Builder<T> extends AzBlendSpace.Builder<T, Builder<T>> {

        private final List<Point> points = new ArrayList<>();

        private AzBlendInput<T> inputX;

        private AzBlendInput<T> inputY;

        private Builder(AzAnimator<?, T> animator, String name) {
            super(animator, name);
        }

        public Builder<T> input(AzBlendInput<T> x, AzBlendInput<T> y) {
            this.inputX = x;
            this.inputY = y;
            return this;
        }

        public Builder<T> sample(double x, double y, String animation) {
            points.add(new Point(x, y, animation));
            return this;
        }

        public AzBlendSpace2D<T> build() {
            return new AzBlendSpace2D<>(this);
        }
    }
}
