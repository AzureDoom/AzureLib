package mod.azure.azurelib.core.molang.functions;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;

/**
 * {@code query.approx_eq(a, b, ...)}: 1 if every argument is within float precision of the first, else 0. Useful for
 * comparing values that went through floating point math, where an exact {@code ==} can miss.
 */
public class ApproxEq extends Function {

    /** Single precision epsilon, matching how Bedrock compares Molang values. */
    private static final double EPSILON = 1.1920929E-7;

    public ApproxEq(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double get() {
        double first = this.getArg(0);

        for (int i = 1; i < this.args.length; i++) {
            double other = this.args[i].get();
            double scale = Math.max(1, Math.max(Math.abs(first), Math.abs(other)));

            if (Math.abs(first - other) > EPSILON * scale)
                return 0;
        }

        return 1;
    }
}
