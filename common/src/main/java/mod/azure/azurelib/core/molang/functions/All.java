package mod.azure.azurelib.core.molang.functions;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * {@code query.all(value, a, b, ...)}: 1 if every argument after the first equals the first, else 0.
 */
public class All extends Function {

    public All(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double get() {
        double value = this.getArg(0);

        for (int i = 1; i < this.args.length; i++) {
            if (this.args[i].get() != value)
                return 0;
        }

        return 1;
    }
}
