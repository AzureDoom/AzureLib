package mod.azure.azurelib.core.math.functions.easing.bounce;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.easing.EasingFunction;

public class EaseInOutBounce extends EasingFunction {

    public EaseInOutBounce(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    protected double ease(double t) {
        return t < 0.5
            ? (1 - bounceOut(1 - 2 * t)) / 2
            : (1 + bounceOut(2 * t - 1)) / 2;
    }

    private double bounceOut(double t) {
        double n1 = 7.5625;
        double d1 = 2.75;

        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5 / d1) * t + 0.75;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25 / d1) * t + 0.9375;
        } else {
            return n1 * (t -= 2.625 / d1) * t + 0.984375;
        }
    }
}
