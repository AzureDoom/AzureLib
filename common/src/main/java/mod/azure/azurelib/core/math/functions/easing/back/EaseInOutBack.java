package mod.azure.azurelib.core.math.functions.easing.back;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutBack extends EasingFunction {

    private static final double C1 = 1.70158;

    private static final double C2 = C1 * 1.525;

    public EaseInOutBack(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return t < 0.5
            ? (Math.pow(2 * t, 2) * ((C2 + 1) * 2 * t - C2)) / 2
            : (Math.pow(2 * t - 2, 2) * ((C2 + 1) * (t * 2 - 2) + C2) + 2) / 2;
    }
}
