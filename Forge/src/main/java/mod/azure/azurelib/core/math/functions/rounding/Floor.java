package mod.azure.azurelib.core.math.functions.rounding;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

public class Floor extends Function {

    public Floor(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        return Math.floor(this.getArg(0));
    }
}
