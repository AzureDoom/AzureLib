package mod.azure.azurelib.core.molang.functions.query;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * Base class for query functions that read game state through the current {@link MolangQueryContext}.
 * <p>
 * These live outside {@code mod.azure.azurelib.core.molang.functions} on purpose: functions in that package are folded
 * to a constant at parse time when all their arguments are constant, which would freeze a call like
 * {@code query.position(1)} to whatever it returned while the animation was loading.
 * </p>
 */
public abstract class ContextQueryFunction extends Function {

    protected ContextQueryFunction(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public final double get() {
        return evaluate(MolangQueryContext.current());
    }

    protected abstract double evaluate(MolangQueryContext context);

    /** The argument at {@code index} as an integer (axis, slot, hand). */
    protected int intArg(int index) {
        return (int) this.getArg(index);
    }
}
