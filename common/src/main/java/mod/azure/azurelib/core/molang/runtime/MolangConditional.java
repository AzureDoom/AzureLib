package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;

/**
 * {@code condition ? a : b} where a branch may produce a string, array or entity reference, so the chosen branch's
 * value passes through intact ({@code t.slot = q.is_sneaking ? 'off_hand' : 'main_hand'}). Purely numeric conditionals
 * use {@link mod.azure.azurelib.core.math.Ternary}.
 */
public final class MolangConditional implements MolangObjectValue {

    private IValue condition;

    private IValue ifTrue;

    private IValue ifFalse;

    public MolangConditional(IValue condition, IValue ifTrue, IValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public double get() {
        return condition.get() != 0 ? ifTrue.get() : ifFalse.get();
    }

    @Override
    public void evaluate(MolangSlot out) {
        MolangSlot.evaluate(condition.get() != 0 ? ifTrue : ifFalse, out);
    }

    @Override
    public IValue simplify() {
        this.condition = condition.simplify();
        this.ifTrue = ifTrue.simplify();
        this.ifFalse = ifFalse.simplify();

        if (condition instanceof Constant) {
            return condition.get() != 0 ? ifTrue : ifFalse;
        }

        return this;
    }

    @Override
    public String toString() {
        return condition + " ? " + ifTrue + " : " + ifFalse;
    }
}
