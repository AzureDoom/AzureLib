package mod.azure.azurelib.core.math.functions.easing.elastic;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutElastic extends EasingFunction {

    private static final double C5 = (2 * Math.PI) / 4.5;

    public EaseInOutElastic(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        if (t == 0)
            return 0;
        if (t == 1)
            return 1;
        return t < 0.5
            ? -(Math.pow(2, 20 * t - 10) * Math.sin((20 * t - 11.125) * C5)) / 2
            : (Math.pow(2, -20 * t + 10) * Math.sin((20 * t - 11.125) * C5)) / 2 + 1;
    }
}
