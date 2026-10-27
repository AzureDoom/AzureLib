package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.Variable;

/**
 * {@code target = value}: evaluates {@code value}, stores it in {@code target} and evaluates to it, so assignments can
 * chain ({@code t.a = t.b = 1}). Never folded at parse time, since storing is a side effect.
 */
public final class MolangAssignment implements IValue {

    private final Variable target;

    private IValue value;

    public MolangAssignment(Variable target, IValue value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public double get() {
        var result = value.get();
        target.set(result);
        return result;
    }

    @Override
    public IValue simplify() {
        this.value = value.simplify();
        return this;
    }

    public Variable target() {
        return target;
    }

    @Override
    public String toString() {
        return target.getName() + " = " + value;
    }
}
