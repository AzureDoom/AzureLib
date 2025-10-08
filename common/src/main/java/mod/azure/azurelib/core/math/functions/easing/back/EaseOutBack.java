package mod.azure.azurelib.core.math.functions.easing.back;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutBack extends EasingFunction {

    private static final double C1 = 1.70158;

    private static final double C3 = C1 + 1;

    public EaseOutBack(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return 1 + C3 * Math.pow(t - 1, 3) + C1 * Math.pow(t - 1, 2);
    }
}
