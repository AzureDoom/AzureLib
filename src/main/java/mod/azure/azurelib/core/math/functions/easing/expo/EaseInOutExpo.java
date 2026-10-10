package mod.azure.azurelib.core.math.functions.easing.expo;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutExpo extends EasingFunction {

    public EaseInOutExpo(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        if (t == 0)
            return 0;
        if (t == 1)
            return 1;
        return t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2;
    }
}
