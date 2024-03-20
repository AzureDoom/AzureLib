package mod.azure.azurelib.core.math.functions.rounding;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

public class Ceil extends Function {

    public Ceil(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    public double get() {
        return Math.ceil(this.getArg(0));
    }
}
