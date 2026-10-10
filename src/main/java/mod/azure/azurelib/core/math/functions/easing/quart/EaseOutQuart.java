package mod.azure.azurelib.core.math.functions.easing.quart;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutQuart extends EasingFunction {

    public EaseOutQuart(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return 1 - Math.pow(1 - t, 4);
    }
}
