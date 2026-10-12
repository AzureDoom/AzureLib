package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code query.movement_direction(axis)}: the entity's normalized movement direction on the given axis.
 */
public class MovementDirection extends ContextQueryFunction {

    public MovementDirection(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 1;
    }

    @Override
    protected double evaluate(MolangQueryContext context) {
        return context.movementDirection(intArg(0));
    }
}
