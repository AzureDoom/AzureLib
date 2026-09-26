package mod.azure.azurelib.common.animation.controller;

/**
 * A linear fade of a controller's blend weight over a number of animator ticks. The clock starts on the first
 * {@link #update} after {@link #start}, so a fade requested between frames (e.g. from a network packet) begins on the
 * next rendered frame rather than jumping ahead.
 */
public final class AzWeightFade {

    private boolean active;

    private double fromWeight;

    private double toWeight;

    private double lengthTicks;

    private double startTick = Double.NaN;

    public void start(double fromWeight, double toWeight, double lengthTicks) {
        this.active = true;
        this.fromWeight = fromWeight;
        this.toWeight = toWeight;
        this.lengthTicks = lengthTicks;
        this.startTick = Double.NaN;
    }

    public void cancel() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * Advances the fade to {@code nowTick} and returns the weight for this frame. The fade ends, and stays at the
     * target, once its length has elapsed.
     */
    public double update(double nowTick) {
        if (!active) {
            return toWeight;
        }

        if (Double.isNaN(startTick)) {
            startTick = nowTick;
        }

        var progress = (nowTick - startTick) / lengthTicks;

        if (progress >= 1) {
            active = false;
            return toWeight;
        }

        return fromWeight + (toWeight - fromWeight) * Math.max(0, progress);
    }
}
