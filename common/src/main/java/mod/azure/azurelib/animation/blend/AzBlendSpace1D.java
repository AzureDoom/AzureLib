package mod.azure.azurelib.animation.blend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import mod.azure.azurelib.animation.AzAnimator;

/**
 * A blend space over one input, such as movement speed or aim pitch. Samples sit at points along the input; the input
 * is clamped to their range and blends the two samples around it.
 *
 * <pre>{@code
 *
 * public void registerControllers(AzAnimationControllerContainer<MyEntity> container) {
 *     AzBlendSpace1D.builder(this, "locomotion")
 *         .input((entity, partialTick) -> entity.getDeltaMovement().horizontalDistance() * 20)
 *         .sample(0, "idle")
 *         .sample(2, "walk")
 *         .sample(6, "run")
 *         .smoothing(3)
 *         .build()
 *         .addTo(container);
 *     container.add(attackController);
 * }
 * }</pre>
 *
 * @param <T> the animatable type
 */
public final class AzBlendSpace1D<T> extends AzBlendSpace<T> {

    private final double[] positions;

    private final AzBlendInput<T> input;

    private double manualInput;

    private double smoothedInput = Double.NaN;

    private AzBlendSpace1D(Builder<T> builder, List<Point> points) {
        super(builder.animator, builder.name, builder.settings, points.stream().map(Point::animation).toList());
        this.positions = points.stream().mapToDouble(Point::position).toArray();
        this.input = builder.input;
    }

    public static <T> Builder<T> builder(AzAnimator<?, T> animator, String name) {
        return new Builder<>(animator, name);
    }

    public void setInput(double value) {
        this.manualInput = value;
    }

    public double input() {
        return Double.isNaN(smoothedInput) ? manualInput : smoothedInput;
    }

    @Override
    protected void computeWeights(T animatable, float partialTick, double smoothing, double[] dest) {
        double raw = input == null ? manualInput : input.get(animatable, partialTick);
        smoothedInput = approach(smoothedInput, raw, smoothing);
        AzBlendWeights.linear(positions, smoothedInput, dest);
    }

    private static final class Point {

        private final double position;

        private final String animation;

        private Point(
            double position,
            String animation
        ) {
            this.position = position;
            this.animation = animation;
        }

        public double position() {
            return position;
        }

        public String animation() {
            return animation;
        }
    }

    public static final class Builder<T> extends AzBlendSpace.Builder<T, Builder<T>> {

        private final List<Point> points = new ArrayList<>();

        private AzBlendInput<T> input;

        private Builder(AzAnimator<?, T> animator, String name) {
            super(animator, name);
        }

        public Builder<T> input(AzBlendInput<T> input) {
            this.input = input;
            return this;
        }

        public Builder<T> sample(double position, String animation) {
            points.add(new Point(position, animation));
            return this;
        }

        public AzBlendSpace1D<T> build() {
            ArrayList<Point> sorted = new ArrayList<>(points);
            sorted.sort(Comparator.comparingDouble(Point::position));
            return new AzBlendSpace1D<>(this, sorted);
        }
    }
}
