package mod.azure.azurelib.core.math.functions.easing.elastic;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutElastic extends EasingFunction {

    private static final double C4 = (2 * Math.PI) / 3;

    public EaseOutElastic(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        if (t == 0)
            return 0;
        if (t == 1)
            return 1;
        return Math.pow(2, -10 * t) * Math.sin((t - 0.1) * C4) + 1;
    }
}
