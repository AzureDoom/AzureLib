package mod.azure.azurelib.animation.controller.keyframe;

public class AzKeyframeLocation<T extends AzKeyframe<?>> {

    private final T keyframe;

    private final double startTick;

    /**
     * A named pair object that stores a {@link AzKeyframe} and a double representing a temporally placed
     * {@code Keyframe}
     *
     * @param keyframe  The {@code Keyframe} at the tick time
     * @param startTick The animation tick time at the start of this {@code Keyframe}
     */
    public AzKeyframeLocation(T keyframe, double startTick) {
        this.keyframe = keyframe;
        this.startTick = startTick;
    }

    public T keyframe() {
        return keyframe;
    }

    public double startTick() {
        return startTick;
    }

    @Override
    public String toString() {
        return "AzKeyframeLocation{" +
            "keyframe=" + keyframe +
            ", startTick=" + startTick +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        AzKeyframeLocation<?> that = (AzKeyframeLocation<?>) o;

        if (Double.compare(that.startTick, startTick) != 0)
            return false;
        return keyframe.equals(that.keyframe);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = keyframe.hashCode();
        temp = Double.doubleToLongBits(startTick);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
