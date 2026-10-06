package mod.azure.azurelib.common.animation.easing;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;

import mod.azure.azurelib.common.animation.controller.keyframe.AzAnimationPoint;
import mod.azure.azurelib.common.animation.easing.bedrock_easings.BezierEasing;
import mod.azure.azurelib.core.utils.Interpolations;

/**
 * AzureLib's built-in easing types.
 * <p>
 * Easings whose curve never depends on an argument (sine, quad, cubic, ...) are registered as stateless, so their curve
 * is built once here instead of on first use. Parameterized easings (step, back, elastic, bounce) build one curve per
 * argument value, which is baked into each keyframe when the animation loads.
 * </p>
 */
public class AzEasingTypes {

    /** Catmull-Rom's fallback curve when a keyframe has no neighbor data. Stateless, so built once. */
    private static final Double2DoubleFunction CATMULL_ROM_FALLBACK = AzEasingUtil.easeInOut(AzEasingUtil::catmullRom);

    public static final AzEasingType NONE = AzEasingTypeRegistry.registerStateless(
        "none",
        AzEasingUtil.easeIn(AzEasingUtil::linear)
    );

    public static final AzEasingType LINEAR = AzEasingTypeRegistry.register("linear", NONE);

    public static final AzEasingType STEP = AzEasingTypeRegistry.register(
        "step",
        value -> AzEasingUtil.easeIn(AzEasingUtil.step(value))
    );

    public static final AzEasingType EASE_IN_SINE = AzEasingTypeRegistry.registerStateless(
        "easeinsine",
        AzEasingUtil.easeIn(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_OUT_SINE = AzEasingTypeRegistry.registerStateless(
        "easeoutsine",
        AzEasingUtil.easeOut(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_IN_OUT_SINE = AzEasingTypeRegistry.registerStateless(
        "easeinoutsine",
        AzEasingUtil.easeInOut(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_IN_QUAD = AzEasingTypeRegistry.registerStateless(
        "easeinquad",
        AzEasingUtil.easeIn(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_OUT_QUAD = AzEasingTypeRegistry.registerStateless(
        "easeoutquad",
        AzEasingUtil.easeOut(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_IN_OUT_QUAD = AzEasingTypeRegistry.registerStateless(
        "easeinoutquad",
        AzEasingUtil.easeInOut(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_IN_CUBIC = AzEasingTypeRegistry.registerStateless(
        "easeincubic",
        AzEasingUtil.easeIn(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_OUT_CUBIC = AzEasingTypeRegistry.registerStateless(
        "easeoutcubic",
        AzEasingUtil.easeOut(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_IN_OUT_CUBIC = AzEasingTypeRegistry.registerStateless(
        "easeinoutcubic",
        AzEasingUtil.easeInOut(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_IN_QUART = AzEasingTypeRegistry.registerStateless(
        "easeinquart",
        AzEasingUtil.easeIn(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_OUT_QUART = AzEasingTypeRegistry.registerStateless(
        "easeoutquart",
        AzEasingUtil.easeOut(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_IN_OUT_QUART = AzEasingTypeRegistry.registerStateless(
        "easeinoutquart",
        AzEasingUtil.easeInOut(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_IN_QUINT = AzEasingTypeRegistry.registerStateless(
        "easeinquint",
        AzEasingUtil.easeIn(AzEasingUtil.pow(5))
    );

    public static final AzEasingType EASE_OUT_QUINT = AzEasingTypeRegistry.registerStateless(
        "easeoutquint",
        AzEasingUtil.easeOut(AzEasingUtil.pow(5))
    );

    public static final AzEasingType EASE_IN_OUT_QUINT = AzEasingTypeRegistry.registerStateless(
        "easeinoutquint",
        AzEasingUtil.easeInOut(AzEasingUtil.pow(5))
    );

    public static final AzEasingType EASE_IN_EXPO = AzEasingTypeRegistry.registerStateless(
        "easeinexpo",
        AzEasingUtil.easeIn(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_OUT_EXPO = AzEasingTypeRegistry.registerStateless(
        "easeoutexpo",
        AzEasingUtil.easeOut(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_IN_OUT_EXPO = AzEasingTypeRegistry.registerStateless(
        "easeinoutexpo",
        AzEasingUtil.easeInOut(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_IN_CIRC = AzEasingTypeRegistry.registerStateless(
        "easeincirc",
        AzEasingUtil.easeIn(AzEasingUtil::circle)
    );

    public static final AzEasingType EASE_OUT_CIRC = AzEasingTypeRegistry.registerStateless(
        "easeoutcirc",
        AzEasingUtil.easeOut(AzEasingUtil::circle)
    );

    public static final AzEasingType EASE_IN_OUT_CIRC = AzEasingTypeRegistry.registerStateless(
        "easeinoutcirc",
        AzEasingUtil.easeInOut(AzEasingUtil::circle)
    );

    public static final AzEasingType EASE_IN_BACK = AzEasingTypeRegistry.register(
        "easeinback",
        value -> AzEasingUtil.easeIn(AzEasingUtil.back(value))
    );

    public static final AzEasingType EASE_OUT_BACK = AzEasingTypeRegistry.register(
        "easeoutback",
        value -> AzEasingUtil.easeOut(AzEasingUtil.back(value))
    );

    public static final AzEasingType EASE_IN_OUT_BACK = AzEasingTypeRegistry.register(
        "easeinoutback",
        value -> AzEasingUtil.easeInOut(AzEasingUtil.back(value))
    );

    public static final AzEasingType EASE_IN_ELASTIC = AzEasingTypeRegistry.register(
        "easeinelastic",
        value -> AzEasingUtil.easeIn(AzEasingUtil.elastic(value))
    );

    public static final AzEasingType EASE_OUT_ELASTIC = AzEasingTypeRegistry.register(
        "easeoutelastic",
        value -> AzEasingUtil.easeOut(AzEasingUtil.elastic(value))
    );

    public static final AzEasingType EASE_IN_OUT_ELASTIC = AzEasingTypeRegistry.register(
        "easeinoutelastic",
        value -> AzEasingUtil.easeInOut(AzEasingUtil.elastic(value))
    );

    public static final AzEasingType EASE_IN_BOUNCE = AzEasingTypeRegistry.register(
        "easeinbounce",
        value -> AzEasingUtil.easeIn(AzEasingUtil.bounce(value))
    );

    public static final AzEasingType EASE_OUT_BOUNCE = AzEasingTypeRegistry.register(
        "easeoutbounce",
        value -> AzEasingUtil.easeOut(AzEasingUtil.bounce(value))
    );

    public static final AzEasingType EASE_IN_OUT_BOUNCE = AzEasingTypeRegistry.register(
        "easeinoutbounce",
        value -> AzEasingUtil.easeInOut(AzEasingUtil.bounce(value))
    );

    // Bedrock Animation Types
    /**
     * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
     */
    public static final AzEasingType BEZIER = AzEasingTypeRegistry.register(
        "bezier",
        new BezierEasing() {

            @Override
            public String name() {
                return "Bezier";
            }

            @Override
            public boolean isEasingBefore() {
                return true;
            }
        }
    );

    /**
     * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
     */
    public static final AzEasingType BEZIER_AFTER = AzEasingTypeRegistry.register(
        "bezier_after",
        new BezierEasing() {

            @Override
            public String name() {
                return "Bezier After";
            }

            @Override
            public boolean isEasingBefore() {
                return false;
            }
        }
    );

    /**
     * <b>Author:</b> <a href="https://github.com/ZigyTheBird">ZigyTheBird</a>
     */
    public static final AzEasingType CATMULLROM = AzEasingTypeRegistry.register(
        "catmullrom",
        new AzEasingType() {

            @Override
            public String name() {
                return "Catmull-Rom";
            }

            @Override
            public Double2DoubleFunction buildTransformer(Double value) {
                return CATMULL_ROM_FALLBACK;
            }

            @Override
            public double apply(AzAnimationPoint animationPoint, Double easingValue, double lerpValue) {
                if (animationPoint.currentTick() >= animationPoint.transitionLength()) {
                    return animationPoint.animationEndValue();
                }

                var keyframe = animationPoint.keyframe();

                if (keyframe == null) {
                    return Interpolations.lerp(
                        animationPoint.animationStartValue(),
                        animationPoint.animationEndValue(),
                        CATMULL_ROM_FALLBACK.get(lerpValue)
                    );
                }

                var easingArgs = keyframe.easingArgs();

                if (easingArgs.size() < 2) {
                    return Interpolations.lerp(
                        animationPoint.animationStartValue(),
                        animationPoint.animationEndValue(),
                        CATMULL_ROM_FALLBACK.get(lerpValue)
                    );
                }

                return AzEasingUtil.catmullRom(
                    lerpValue,
                    easingArgs.get(0).get(),
                    animationPoint.animationStartValue(),
                    animationPoint.animationEndValue(),
                    easingArgs.get(1).get()
                );
            }

            @Override
            public boolean usesKeyframeData() {
                return true;
            }
        }

    );

    public static AzEasingType random() {
        var collection = AzEasingTypeRegistry.getValues();

        return collection.stream()
            .skip((int) (collection.size() * Math.random()))
            .findFirst()
            .orElse(null);
    }
}
