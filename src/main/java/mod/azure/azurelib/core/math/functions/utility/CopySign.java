package mod.azure.azurelib.core.math.functions.utility;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * Copy sign function Returns a value with the magnitude of A and the sign of B
 */
public class CopySign extends Function {

    public CopySign(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double get() {
        double magnitude = Math.abs(this.getArg(0));
        double sign = this.getArg(1);

        return sign < 0 ? -magnitude : magnitude;
    }
}
