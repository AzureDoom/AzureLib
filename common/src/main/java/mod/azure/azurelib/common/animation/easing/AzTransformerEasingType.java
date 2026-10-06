package mod.azure.azurelib.common.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import mod.azure.azurelib.common.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.common.animation.controller.keyframe.AzKeyframe;
import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.utils.Interpolations;

/**
 * The {@link AzEasingType} created by {@link AzEasingTypeRegistry#register(String, Function)} and
 * {@link AzEasingTypeRegistry#registerStateless(String, Double2DoubleFunction)}.
 * <p>
 * Stateless types (sine, quad, cubic, ...) hold one curve, built up front and shared by every keyframe. Parameterized
 * types (back, elastic, bounce, step) build a curve per argument value and reuse it. Either way, when a keyframe's
 * argument is a constant, its curve is also baked into the keyframe channel when the animation loads (see
 * {@link #resolveTransformer}), so playback skips the lookup entirely.
 * </p>
 */
final class AzTransformerEasingType implements AzEasingType {

    /** Safety cap for argument values coming from somewhere other than animation files. */
    private static final int MAX_CACHED_ARGUMENTS = 256;

    private final String name;

    /**
     * Builds the curve for an argument. Never null: stateless types get a factory that returns their one curve, so
     * every code path can call it without a null check.
     */
    private final Function<Double, Double2DoubleFunction> factory;

    /** The curve for stateless types, set up front; {@code null} for parameterized types. */
    @Nullable
    private final Double2DoubleFunction statelessTransformer;

    /** A parameterized type's curve with no argument. Lazily created; a racing duplicate build is harmless. */
    @Nullable
    private volatile Double2DoubleFunction defaultTransformer;

    /** A parameterized type's curves by argument. Copy-on-write, so the per-frame read is a lock-free lookup. */
    private volatile Double2ObjectOpenHashMap<Double2DoubleFunction> transformersByArgument =
        new Double2ObjectOpenHashMap<>(0);

    AzTransformerEasingType(String name, Function<Double, Double2DoubleFunction> factory) {
        this.name = name;
        this.factory = factory;
        this.statelessTransformer = null;
    }

    AzTransformerEasingType(String name, @Nullable Double2DoubleFunction statelessTransformer) {
        this.name = name;
        this.factory = argument -> statelessTransformer;
        this.statelessTransformer = statelessTransformer;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Double2DoubleFunction buildTransformer(Double value) {
        if (statelessTransformer != null)
            return statelessTransformer;

        return value == null ? defaultTransformer() : transformer(value);
    }

    @Override
    public @Nullable Double2DoubleFunction resolveTransformer(AzKeyframe<?> keyframe) {
        if (statelessTransformer != null)
            return statelessTransformer;

        var args = keyframe.easingArgs();

        if (args.isEmpty())
            return defaultTransformer();

        // Only fixed arguments can be resolved ahead of time; anything that can change stays dynamic.
        return args.get(0) instanceof Constant constant && constant.isFixed() ? transformer(constant.get()) : null;
    }

    private Double2DoubleFunction defaultTransformer() {
        var transformer = defaultTransformer;

        if (transformer == null) {
            transformer = factory.apply(null);
            defaultTransformer = transformer;
        }

        return transformer;
    }

    private Double2DoubleFunction transformer(double argument) {
        var cache = transformersByArgument;
        var transformer = cache.get(argument);

        if (transformer == null) {
            transformer = factory.apply(argument);

            if (cache.size() < MAX_CACHED_ARGUMENTS) {
                var updated = new Double2ObjectOpenHashMap<>(cache);
                updated.put(argument, transformer);
                transformersByArgument = updated;
            }
        }

        return transformer;
    }

    /**
     * Same result as the default {@link AzEasingType#apply(AzAnimationPoint)}, without boxing, and using the curve
     * baked into the point when it belongs to this type.
     */
    @Override
    public double apply(AzAnimationPoint animationPoint) {
        if (animationPoint.currentTick() >= animationPoint.transitionLength())
            return (float) animationPoint.animationEndValue();

        return Interpolations.lerp(
            animationPoint.animationStartValue(),
            animationPoint.animationEndValue(),
            transformerFor(animationPoint).get(animationPoint.currentTick() / animationPoint.transitionLength())
        );
    }

    private Double2DoubleFunction transformerFor(AzAnimationPoint animationPoint) {
        if (statelessTransformer != null)
            return statelessTransformer;

        var keyframe = animationPoint.keyframe();

        if (keyframe == null)
            return defaultTransformer();

        // A baked curve is resolved for the keyframe's own easing type. Only use it when that's this type; when a
        // controller overrides the easing, this type evaluates the keyframe's arguments itself.
        var baked = animationPoint.bakedTransformer();

        if (baked != null && keyframe.easingType() == this)
            return baked;

        var args = keyframe.easingArgs();

        return args.isEmpty() ? defaultTransformer() : transformer(args.get(0).get());
    }
}
