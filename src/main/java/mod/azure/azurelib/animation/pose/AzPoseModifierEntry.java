package mod.azure.azurelib.animation.pose;

import java.util.Objects;

import mod.azure.azurelib.animation.controller.AzBoneMask;
import mod.azure.azurelib.util.math.Mth;

/**
 * A registered {@link AzPoseModifier} and its settings. Settings are shared by every animatable using the animator; the
 * live weight of one animatable is controlled through its {@link AzPoseLayer}.
 *
 * @param <T> the animatable type
 */
public final class AzPoseModifierEntry<T> {

    private final AzPoseModifierRegistry<T> registry;

    private final String id;

    private final AzPoseModifier<T> modifier;

    private final int order;

    private int priority;

    private AzBoneMask mask = AzBoneMask.ALL;

    private double defaultWeight = 1;

    AzPoseModifierEntry(AzPoseModifierRegistry<T> registry, String id, AzPoseModifier<T> modifier, int order) {
        this.registry = registry;
        this.id = Objects.requireNonNull(id, "id");
        this.modifier = Objects.requireNonNull(modifier, "modifier");
        this.order = order;
    }

    /**
     * Modifiers run in ascending priority, then in the order they were added. Later modifiers see and build on what
     * earlier ones wrote. Defaults to 0.
     */
    public AzPoseModifierEntry<T> priority(int priority) {
        this.priority = priority;
        registry.markDirty();
        return this;
    }

    /**
     * Limits which bones this modifier may write, exactly like a controller's mask: a named bone includes its children.
     * Writes to bones outside the mask are ignored; reads still work. Defaults to {@link AzBoneMask#ALL}.
     */
    public AzPoseModifierEntry<T> mask(AzBoneMask mask) {
        this.mask = Objects.requireNonNull(mask, "mask");
        return this;
    }

    /**
     * The weight an animatable starts with, from 0 (off) to 1 (full effect). Defaults to 1. Change it per animatable
     * with {@link AzPoseLayer#setWeight} or {@link AzPoseLayer#fadeWeight}.
     */
    public AzPoseModifierEntry<T> defaultWeight(double weight) {
        this.defaultWeight = Mth.clamp(weight, 0, 1);
        return this;
    }

    public String id() {
        return id;
    }

    public AzPoseModifier<T> modifier() {
        return modifier;
    }

    public int priority() {
        return priority;
    }

    public AzBoneMask mask() {
        return mask;
    }

    public double defaultWeight() {
        return defaultWeight;
    }

    int order() {
        return order;
    }
}
