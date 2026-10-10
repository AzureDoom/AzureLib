package mod.azure.azurelib.core.math.functions.easing.quart;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutQuart extends EasingFunction {

    public EaseInOutQuart(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return t < 0.5 ? 8 * t * t * t * t : 1 - Math.pow(-2 * t + 2, 4) / 2;
    }
}
