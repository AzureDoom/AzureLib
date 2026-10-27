package mod.azure.azurelib.animation.dispatch.command.sequence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * What an {@link AzSequencePlayer} needs to know to place stage-anchored events ({@link AzSequenceEvent#isAnchored()})
 * where the animation will actually be: the controller's playback settings and the length of each animation.
 * <p>
 * The playback values here are the <i>controller's</i>; a stage that sets its own speed, transition length, start
 * offset, repeat count, freeze point or direction (via {@code AzSequence.Builder#then(name, behavior, props -> ...)})
 * uses its own value for that stage, exactly like the controller does.
 * <p>
 * Animation files only exist on the client, so on the server, tell the timing how long each animation is:
 *
 * <pre>
 *
 * {
 *     &#64;code
 *
 *     static final AzSequenceTiming SLAM_TIMING = AzSequenceTiming.builder()
 *         .transitionLength(4) // whatever the controller was built with
 *         .length("windup", 20)
 *         .length("strike", 12)
 *         .build();
 * }
 * </pre>
 *
 * On the client, {@code AzAnimationController#sequenceTiming()} builds one from a live controller, reading lengths
 * straight from the animation file.
 * <p>
 * An anchored event only needs the lengths of the stages before it, plus its own stage's length when it fires at the
 * stage's end or the stage plays in reverse. Events whose time can't be worked out are skipped with a warning rather
 * than fired at a guessed time.
 */
public final class AzSequenceTiming {

    /** Speed 1, no transition, no offsets, no lengths known. */
    public static final AzSequenceTiming DEFAULT = builder().build();

    private final double animationSpeed;

    private final double transitionLength;

    private final double startTickOffset;

    private final double freezeTickOffset;

    private final double repeatXTimes;

    private final boolean reversing;

    private final Map<String, Double> lengths;

    private final ToDoubleFunction<String> lengthResolver;

    private AzSequenceTiming(Builder builder) {
        this.animationSpeed = builder.animationSpeed;
        this.transitionLength = builder.transitionLength;
        this.startTickOffset = builder.startTickOffset;
        this.freezeTickOffset = builder.freezeTickOffset;
        this.repeatXTimes = builder.repeatXTimes;
        this.reversing = builder.reversing;
        this.lengths = Collections.unmodifiableMap(new HashMap<>(builder.lengths));
        this.lengthResolver = builder.lengthResolver;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @return a builder pre-filled with this timing's values
     */
    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.animationSpeed = animationSpeed;
        builder.transitionLength = transitionLength;
        builder.startTickOffset = startTickOffset;
        builder.freezeTickOffset = freezeTickOffset;
        builder.repeatXTimes = repeatXTimes;
        builder.reversing = reversing;
        builder.lengths.putAll(lengths);
        builder.lengthResolver = lengthResolver;
        return builder;
    }

    /**
     * The length of an animation in ticks at speed 1, or {@link Double#NaN} if it isn't known. Explicit
     * {@link Builder#length lengths} win over the {@link Builder#lengthResolver resolver}.
     */
    public double length(String animationName) {
        Double explicit = lengths.get(animationName);

        if (explicit != null) {
            return explicit;
        }

        if (lengthResolver != null) {
            double resolved = lengthResolver.applyAsDouble(animationName);
            return resolved >= 0 ? resolved : Double.NaN;
        }

        return Double.NaN;
    }

    public double animationSpeed() {
        return animationSpeed;
    }

    public double transitionLength() {
        return transitionLength;
    }

    public double startTickOffset() {
        return startTickOffset;
    }

    public double freezeTickOffset() {
        return freezeTickOffset;
    }

    public double repeatXTimes() {
        return repeatXTimes;
    }

    public boolean isReversing() {
        return reversing;
    }

    public static final class Builder {

        private double animationSpeed = 1;

        private double transitionLength = 0;

        private double startTickOffset = 0;

        private double freezeTickOffset = 0;

        private double repeatXTimes = 1;

        private boolean reversing = false;

        private final Map<String, Double> lengths = new HashMap<>();

        private ToDoubleFunction<String> lengthResolver;

        private Builder() {}

        /** The controller's animation speed (default 1). */
        public Builder animationSpeed(double animationSpeed) {
            this.animationSpeed = animationSpeed;
            return this;
        }

        /** The controller's transition length in ticks (default 0). */
        public Builder transitionLength(double transitionLength) {
            this.transitionLength = Math.max(0, transitionLength);
            return this;
        }

        /** The controller's start tick offset (default 0). */
        public Builder startTickOffset(double startTickOffset) {
            this.startTickOffset = Math.max(0, startTickOffset);
            return this;
        }

        /** The controller's freeze point, used by freeze_on_frame stages without their own (default none). */
        public Builder freezeTickOffset(double freezeTickOffset) {
            this.freezeTickOffset = freezeTickOffset;
            return this;
        }

        /** The controller's repeat count, used by repeat_x_times stages without their own (default 1). */
        public Builder repeatXTimes(double repeatXTimes) {
            this.repeatXTimes = repeatXTimes;
            return this;
        }

        /** The controller's direction, used by stages without their own (default forward). */
        public Builder reversing(boolean reversing) {
            this.reversing = reversing;
            return this;
        }

        /** The length of {@code animationName} in ticks at speed 1, as shown in Blockbench. */
        public Builder length(String animationName, double lengthTicks) {
            Objects.requireNonNull(animationName, "animationName");

            if (!(lengthTicks >= 0)) {
                throw new IllegalArgumentException(
                    "Length of '" + animationName + "' must be >= 0, was " + lengthTicks
                );
            }

            this.lengths.put(animationName, lengthTicks);
            return this;
        }

        /** Adds every entry of {@code lengthsByAnimation}; see {@link #length(String, double)}. */
        public Builder lengths(Map<String, ? extends Number> lengthsByAnimation) {
            lengthsByAnimation.forEach((name, length) -> length(name, length.doubleValue()));
            return this;
        }

        /**
         * Looks lengths up on demand, for animations without an explicit {@link #length}. Return a negative value or
         * {@link Double#NaN} for unknown animations.
         */
        public Builder lengthResolver(ToDoubleFunction<String> lengthResolver) {
            this.lengthResolver = lengthResolver;
            return this;
        }

        public AzSequenceTiming build() {
            return new AzSequenceTiming(this);
        }
    }
}
