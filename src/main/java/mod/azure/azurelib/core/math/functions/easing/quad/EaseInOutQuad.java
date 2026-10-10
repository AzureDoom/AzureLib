package mod.azure.azurelib.core.math.functions.easing.quad;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutQuad extends EasingFunction {

    public EaseInOutQuad(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }
}
