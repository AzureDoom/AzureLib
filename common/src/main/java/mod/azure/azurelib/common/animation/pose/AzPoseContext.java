package mod.azure.azurelib.common.animation.pose;

import java.util.function.Supplier;

import mod.azure.azurelib.common.animation.AzAnimationContext;

/**
 * What a {@link AzPoseModifier} sees while it runs: the animatable, frame timing, its own weight and per-animatable
 * state, and {@link AzPoseBone} handles for the model's bones. One instance per animatable, reused every frame; do not
 * keep it past {@link AzPoseModifier#apply}.
 *
 * @param <T> the animatable type
 */
public final class AzPoseContext<T> {

    private final AzPoseLayer<T> layer;

    private T animatable;

    private float partialTick;

    private double animTime;

    private double deltaTicks;

    private boolean discontinuous;

    AzPoseContext(AzPoseLayer<T> layer) {
        this.layer = layer;
    }

    void begin(T animatable, float partialTick, double animTime, double deltaTicks) {
        this.animatable = animatable;
        this.partialTick = partialTick;
        this.animTime = animTime;
        this.deltaTicks = deltaTicks;
    }

    void enter(boolean discontinuous) {
        this.discontinuous = discontinuous;
    }

    void end() {
        this.animatable = null;
    }

    public T animatable() {
        return animatable;
    }

    public AzAnimationContext<T> animationContext() {
        return layer.animationContext();
    }

    public float partialTick() {
        return partialTick;
    }

    public double animTime() {
        return animTime;
    }

    public double deltaTicks() {
        return deltaTicks;
    }

    public boolean isDiscontinuous() {
        return discontinuous;
    }

    public String modifierId() {
        return layer.currentEntry().id();
    }

    public float weight() {
        return layer.weight();
    }

    public AzPoseBone bone(String name) {
        return layer.bone(name);
    }

    public <S> S state(Supplier<S> factory) {
        return layer.state(factory);
    }
}
