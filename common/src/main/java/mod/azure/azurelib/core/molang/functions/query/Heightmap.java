package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code query.heightmap(x, z)}: the height of the world surface at the given world position.
 */
public class Heightmap extends ContextQueryFunction {

    public Heightmap(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    protected double evaluate(MolangQueryContext context) {
        return context.heightmap(this.getArg(0), this.getArg(1));
    }
}
