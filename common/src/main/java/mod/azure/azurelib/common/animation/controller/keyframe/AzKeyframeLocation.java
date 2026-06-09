package mod.azure.azurelib.common.animation.controller.keyframe;

/**
 * Mutable named pair that stores a {@link AzKeyframe} and the animation tick time at the start of that keyframe.
 * Previously an immutable record; converted to a mutable class so the single scratch instance on
 * {@link AzAbstractKeyframeExecutor} can be reused every frame without allocating a new object per axis per bone.
 */
public final class AzKeyframeLocation<T extends AzKeyframe<?>> {

    private T keyframe;

    private double startTick;

    public AzKeyframeLocation(T keyframe, double startTick) {
        this.keyframe = keyframe;
        this.startTick = startTick;
    }

    /**
     * Overwrites both fields in-place and returns {@code this} for convenience chaining.
     */
    public AzKeyframeLocation<T> set(T keyframe, double startTick) {
        this.keyframe = keyframe;
        this.startTick = startTick;
        return this;
    }

    public T keyframe() {
        return keyframe;
    }

    public double startTick() {
        return startTick;
    }
}
