package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.Variable;
import mod.azure.azurelib.core.molang.expressions.MolangObjectVariable;

/**
 * {@code target = value}: evaluates {@code value}, stores it in {@code target} and evaluates to it, so assignments can
 * chain ({@code t.a = t.b = 1}). Never folded at parse time, since storing is a side effect.
 * <p>
 * When the value can be a string, array or entity reference and the target is a {@code temp.*} or {@code variable.*},
 * the object is stored as-is, so variables share array and entity references rather than copying them. Assigning an
 * object to a variable that can only hold numbers (a global variable driven by code) stores 0.
 * </p>
 */
public final class MolangAssignment implements MolangObjectValue {

    private final Variable target;

    private IValue value;

    private final MolangSlot slot = new MolangSlot();

    public MolangAssignment(Variable target, IValue value) {
        this.target = target;
        this.value = value;
    }

    @Override
    public double get() {
        if (value instanceof MolangObjectValue) {
            evaluate(slot);
            double result = slot.number();
            slot.clear();
            return result;
        }

        double result = value.get();
        target.set(result);
        return result;
    }

    @Override
    public void evaluate(MolangSlot out) {
        MolangSlot.evaluate(value, out);

        if (!out.isObject()) {
            target.set(out.number());
        } else if (target instanceof MolangObjectVariable objectVariable) {
            objectVariable.setObject(out.object());
        } else {
            target.set(0);
        }
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
