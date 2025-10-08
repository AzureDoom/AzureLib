package mod.azure.azurelib.core.math.functions.utility;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * Sign function Returns -1 for negative values, 0 for zero, and 1 for positive values
 */
public class Sign extends Function {

    public Sign(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        double value = this.getArg(0);

        if (value < 0)
            return -1;
        if (value > 0)
            return 1;
        return 0;
    }
}
