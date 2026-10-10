package mod.azure.azurelib.animation;

/**
 * The {@code AzAnimatorConfig} record encapsulates configuration settings related to the animation system of the
 * AzureLib framework. It provides customizable options for controlling animation behavior and error handling. This
 * configuration is used to determine runtime behaviors such as whether animations should continue while the game is
 * paused, whether the system should throw an error if a bone in the animation structure is missing, and the duration it
 * takes to reset bone transformations.
 *
 * @param boneResetTime                       The specified time duration (in ticks or seconds) for resetting bones to
 *                                            their default transformations when animations are interrupted.
 * @param crashIfBoneMissing                  Specifies whether the system will throw an exception if an expected bone
 *                                            in the animation is not found during runtime.
 * @param shouldPlayAnimationsWhileGamePaused Indicates whether animations should continue playing when the game is
 *                                            paused.
 */
public final class AzAnimatorConfig {

    private final double boneResetTime;

    private final boolean crashIfBoneMissing;

    private final boolean shouldPlayAnimationsWhileGamePaused;

    public AzAnimatorConfig(
        double boneResetTime,
        boolean crashIfBoneMissing,
        boolean shouldPlayAnimationsWhileGamePaused
    ) {
        this.boneResetTime = boneResetTime;
        this.crashIfBoneMissing = crashIfBoneMissing;
        this.shouldPlayAnimationsWhileGamePaused = shouldPlayAnimationsWhileGamePaused;
    }

    public double boneResetTime() {
        return this.boneResetTime;
    }

    public boolean crashIfBoneMissing() {
        return this.crashIfBoneMissing;
    }

    public boolean shouldPlayAnimationsWhileGamePaused() {
        return this.shouldPlayAnimationsWhileGamePaused;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AzAnimatorConfig))
            return false;
        AzAnimatorConfig other = (AzAnimatorConfig) o;
        return Double.compare(this.boneResetTime, other.boneResetTime) == 0
            && this.crashIfBoneMissing == other.crashIfBoneMissing
            && this.shouldPlayAnimationsWhileGamePaused == other.shouldPlayAnimationsWhileGamePaused;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + Double.hashCode(this.boneResetTime);
        result = 31 * result + Boolean.hashCode(this.crashIfBoneMissing);
        result = 31 * result + Boolean.hashCode(this.shouldPlayAnimationsWhileGamePaused);
        return result;
    }

    @Override
    public String toString() {
        return "AzAnimatorConfig[boneResetTime=" + this.boneResetTime + ", crashIfBoneMissing="
            + this.crashIfBoneMissing + ", shouldPlayAnimationsWhileGamePaused="
            + this.shouldPlayAnimationsWhileGamePaused + "]";
    }

    /**
     * Creates a new instance of the {@link Builder} to configure and build an {@code AzAnimatorConfig}.
     *
     * @return A new {@code Builder} instance for constructing an {@code AzAnimatorConfig}.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns a default {@link AzAnimatorConfig} instance with predefined settings. The default configuration typically
     * includes settings such as:
     * <ul>
     * <li>A bone reset time of 1 tick/second.</li>
     * <li>Disabling the feature to crash if a bone is missing.</li>
     * <li>Disabling animations while the game is paused.</li>
     * </ul>
     *
     * @return The default configuration instance of {@code AzAnimatorConfig}, built with default values.
     */
    public static AzAnimatorConfig defaultConfig() {
        return builder().build();
    }

    public static class Builder {

        private double boneResetTime;

        private boolean crashIfBoneMissing;

        private boolean shouldPlayAnimationsWhileGamePaused;

        private Builder() {
            this.boneResetTime = 1;
            this.crashIfBoneMissing = false;
            this.shouldPlayAnimationsWhileGamePaused = false;
        }

        /**
         * Configures the builder to crash if a required bone is missing during animation setup.
         *
         * @return The current Builder instance for method chaining.
         */
        public Builder crashIfBoneMissing() {
            this.crashIfBoneMissing = true;
            return this;
        }

        /**
         * Configures the builder to enable animations to play even when the game is paused.
         *
         * @return The current Builder instance for method chaining.
         */
        public Builder shouldPlayAnimationsWhileGamePaused() {
            this.shouldPlayAnimationsWhileGamePaused = true;
            return this;
        }

        /**
         * Sets the bone reset time duration. This value determines how long it takes to reset bones to their default
         * state after an animation is completed.
         *
         * @param boneResetTime The duration (in seconds) for bone reset time.
         * @return The current Builder instance to allow method chaining.
         */
        public Builder withBoneResetTime(double boneResetTime) {
            this.boneResetTime = boneResetTime;
            return this;
        }

        /**
         * Constructs a new {@link AzAnimatorConfig} instance with the specified configuration parameters defined in the
         * Builder. The configuration includes options for bone reset timing, error handling when bones are missing, and
         * animation playback behavior during game pause state.
         *
         * @return A new {@code AzAnimatorConfig} instance containing the configured settings.
         */
        public AzAnimatorConfig build() {
            return new AzAnimatorConfig(
                boneResetTime,
                crashIfBoneMissing,
                shouldPlayAnimationsWhileGamePaused
            );
        }
    }
}
