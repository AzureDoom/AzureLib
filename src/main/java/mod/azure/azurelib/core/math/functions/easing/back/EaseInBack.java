package mod.azure.azurelib.core.math.functions.easing.back;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInBack extends EasingFunction {

    private static final double C1 = 1.70158;

    private static final double C3 = C1 + 1;

    public EaseInBack(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return C3 * t * t * t - C1 * t * t;
    }
}
