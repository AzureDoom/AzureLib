package mod.azure.azurelib.core.math.functions.easing.sine;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInSine extends EasingFunction {

    public EaseInSine(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return 1 - Math.cos((t * Math.PI) / 2);
    }
}
