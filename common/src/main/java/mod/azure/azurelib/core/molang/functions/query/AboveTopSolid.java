package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code query.above_top_solid(x, z)}: the height just above the highest solid block at the given world position.
 */
public class AboveTopSolid extends ContextQueryFunction {

    public AboveTopSolid(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    protected double evaluate(MolangQueryContext context) {
        return context.aboveTopSolid(this.getArg(0), this.getArg(1));
    }
}
