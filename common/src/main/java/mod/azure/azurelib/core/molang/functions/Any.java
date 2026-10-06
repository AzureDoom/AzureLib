package mod.azure.azurelib.core.molang.functions;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * {@code query.any(value, a, b, ...)}: 1 if any argument after the first equals the first, else 0.
 */
public class Any extends Function {

    public Any(IValue[] values, String name) throws Exception {
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
            if (this.args[i].get() == value)
                return 1;
        }

        return 0;
    }
}
