package mod.azure.azurelib.core.math.functions.easing;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * Base class for easing functions All easing functions take (start, end, t) where t is 0-1
 */
public abstract class EasingFunction extends Function {

    protected EasingFunction(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 3;
    }

    @Override
    public double get() {
        double start = this.getArg(0);
        double end = this.getArg(1);
        double t = this.getArg(2);

        // Clamp t to [0, 1]
        t = Math.max(0, Math.min(1, t));

        double easedT = ease(t);
        return start + (end - start) * easedT;
    }

    /**
     * The easing function to apply to t (0-1)
     *
     * @param t input value from 0 to 1
     * @return eased value
     */
    protected abstract double ease(double t);
}
