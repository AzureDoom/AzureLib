package mod.azure.azurelib.core.math.functions.easing.sine;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseOutSine extends EasingFunction {

    public EaseOutSine(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return Math.sin((t * Math.PI) / 2);
    }
}
