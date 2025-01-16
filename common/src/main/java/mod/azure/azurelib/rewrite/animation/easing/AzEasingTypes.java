package mod.azure.azurelib.rewrite.animation.easing;

public class AzEasingTypes {

    public static final AzEasingType NONE = AzEasingTypeRegistry.register(
        "none",
        value -> AzEasingUtil.easeIn(AzEasingUtil::linear)
    );

    public static final AzEasingType LINEAR = AzEasingTypeRegistry.register("linear", NONE);

    public static final AzEasingType STEP = AzEasingTypeRegistry.register(
        "step",
        value -> AzEasingUtil.easeIn(AzEasingUtil.step(value))
    );

    public static final AzEasingType EASE_IN_SINE = AzEasingTypeRegistry.register(
        "easeinsine",
        value -> AzEasingUtil.easeIn(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_OUT_SINE = AzEasingTypeRegistry.register(
        "easeoutsine",
        value -> AzEasingUtil.easeOut(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_IN_OUT_SINE = AzEasingTypeRegistry.register(
        "easeinoutsine",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::sine)
    );

    public static final AzEasingType EASE_IN_QUAD = AzEasingTypeRegistry.register(
        "easeinquad",
        value -> AzEasingUtil.easeIn(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_OUT_QUAD = AzEasingTypeRegistry.register(
        "easeoutquad",
        value -> AzEasingUtil.easeOut(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_IN_OUT_QUAD = AzEasingTypeRegistry.register(
        "easeinoutquad",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::quadratic)
    );

    public static final AzEasingType EASE_IN_CUBIC = AzEasingTypeRegistry.register(
        "easeincubic",
        value -> AzEasingUtil.easeIn(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_OUT_CUBIC = AzEasingTypeRegistry.register(
        "easeoutcubic",
        value -> AzEasingUtil.easeOut(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_IN_OUT_CUBIC = AzEasingTypeRegistry.register(
        "easeinoutcubic",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::cubic)
    );

    public static final AzEasingType EASE_IN_QUART = AzEasingTypeRegistry.register(
        "easeinquart",
        value -> AzEasingUtil.easeIn(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_OUT_QUART = AzEasingTypeRegistry.register(
        "easeoutquart",
        value -> AzEasingUtil.easeOut(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_IN_OUT_QUART = AzEasingTypeRegistry.register(
        "easeinoutquart",
        value -> AzEasingUtil.easeInOut(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_IN_QUINT = AzEasingTypeRegistry.register(
        "easeinquint",
        value -> AzEasingUtil.easeIn(AzEasingUtil.pow(4))
    );

    public static final AzEasingType EASE_OUT_QUINT = AzEasingTypeRegistry.register(
        "easeoutquint",
        value -> AzEasingUtil.easeOut(AzEasingUtil.pow(5))
    );

    public static final AzEasingType EASE_IN_OUT_QUINT = AzEasingTypeRegistry.register(
        "easeinoutquint",
        value -> AzEasingUtil.easeInOut(AzEasingUtil.pow(5))
    );

    public static final AzEasingType EASE_IN_EXPO = AzEasingTypeRegistry.register(
        "easeinexpo",
        value -> AzEasingUtil.easeIn(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_OUT_EXPO = AzEasingTypeRegistry.register(
        "easeoutexpo",
        value -> AzEasingUtil.easeOut(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_IN_OUT_EXPO = AzEasingTypeRegistry.register(
        "easeinoutexpo",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::exp)
    );

    public static final AzEasingType EASE_IN_CIRC = AzEasingTypeRegistry.register(
        "easeincirc",
        value -> AzEasingUtil.easeIn(AzEasingUtil::circle)
    );

    public static final AzEasingType EASE_OUT_CIRC = AzEasingTypeRegistry.register(
        "easeoutcirc",
        value -> AzEasingUtil.easeOut(AzEasingUtil::circle)
    );

    public static final AzEasingType EASE_IN_OUT_CIRC = AzEasingTypeRegistry.register(
        "easeinoutcirc",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::circle)
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

    public static final AzEasingType CATMULLROM = AzEasingTypeRegistry.register(
        "catmullrom",
        value -> AzEasingUtil.easeInOut(AzEasingUtil::catmullRom)
    );

    public static AzEasingType random() {
        var collection = AzEasingTypeRegistry.getValues();

        return collection.stream()
            .skip((int) (collection.size() * Math.random()))
            .findFirst()
            .orElse(null);
    }
}
