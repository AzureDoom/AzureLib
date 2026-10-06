package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code query.position_delta(axis)}: how far the entity is moving this tick on the given axis.
 */
public class PositionDelta extends ContextQueryFunction {

    public PositionDelta(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    protected double evaluate(MolangQueryContext context) {
        return context.positionDelta(intArg(0));
    }
}
