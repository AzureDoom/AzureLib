package mod.azure.azurelib.core.math.functions.easing.elastic;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInElastic extends EasingFunction {

    private static final double C4 = (2 * Math.PI) / 3;

    public EaseInElastic(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        if (t == 0)
            return 0;
        if (t == 1)
            return 1;
        return -Math.pow(2, 10 * (t - 1)) * Math.sin((t - 1.1) * C4);
    }
}
