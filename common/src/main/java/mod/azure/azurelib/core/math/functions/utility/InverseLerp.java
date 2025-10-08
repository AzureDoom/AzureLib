package mod.azure.azurelib.core.math.functions.utility;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * Inverse lerp function Returns the interpolation factor (0 to 1) that would produce the given value between start and
 * end
 */
public class InverseLerp extends Function {

    public InverseLerp(IValue[] values, String name) throws Exception {
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
        double value = this.getArg(2);

        if (start == end)
            return 0;

        return (value - start) / (end - start);
    }
}
