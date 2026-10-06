package mod.azure.azurelib.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;

import java.util.function.Function;

import mod.azure.azurelib.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.core.utils.Interpolations;

/**
 * The {@link AzEasingType} created by {@link AzEasingTypeRegistry#register(String, Function)}.
 * <p>
 * Easing curves are pure functions of their argument, so instead of rebuilding the curve (and its wrapper lambdas)
 * every time a keyframe is sampled, this builds it once per argument value and reuses it. Argument-free easings (sine,
 * quad, cubic, ...) end up with a single cached curve; parameterized ones (back, elastic, bounce, step) get one per
 * distinct argument found in the loaded animations.
 * </p>
 */
final class AzTransformerEasingType implements AzEasingType {

    private final String name;

    private final Function<Double, Double2DoubleFunction> factory;

    private volatile Double2DoubleFunction defaultTransformer;

    private volatile Double2ObjectOpenHashMap<Double2DoubleFunction> transformersByArgument =
        new Double2ObjectOpenHashMap<>(0);

    AzTransformerEasingType(String name, Function<Double, Double2DoubleFunction> factory) {
        this.name = name;
        this.factory = factory;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Double2DoubleFunction buildTransformer(Double value) {
        return value == null ? defaultTransformer() : transformer(value);
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

            if (cache.size() < 256) {
                var updated = new Double2ObjectOpenHashMap<>(cache);
                updated.put(argument, transformer);
                transformersByArgument = updated;
            }
        }

        return transformer;
    }

    @Override
    public double apply(AzAnimationPoint animationPoint) {
        if (animationPoint.currentTick() >= animationPoint.transitionLength())
            return (float) animationPoint.animationEndValue();

        var keyframe = animationPoint.keyframe();
        var transformer = keyframe != null && !keyframe.easingArgs().isEmpty()
            ? transformer(keyframe.easingArgs().get(0).get())
            : defaultTransformer();

        return Interpolations.lerp(
            animationPoint.animationStartValue(),
            animationPoint.animationEndValue(),
            transformer.get(animationPoint.currentTick() / animationPoint.transitionLength())
        );
    }
}
