package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.expressions.MolangDefinable;

/**
 * {@code left ?? right}: {@code left} if it holds a value, otherwise {@code right}. Only variables can be unset
 * ({@link MolangDefinable}), so {@code v.speed ?? 1} gives 1 until something assigns {@code v.speed}; anything else on
 * the left is always used. A variable holding an entity that has since been collected counts as unset. Strings, arrays
 * and entity references pass through unchanged.
 */
public final class MolangCoalesce implements MolangObjectValue {

    private IValue left;

    private IValue right;

    public MolangCoalesce(IValue left, IValue right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public double get() {
        if (left instanceof MolangDefinable definable && !definable.isDefined()) {
            return right.get();
        }

        return left.get();
    }

    @Override
    public void evaluate(MolangSlot out) {
        if (left instanceof MolangDefinable definable && !definable.isDefined()) {
            MolangSlot.evaluate(right, out);
        } else {
            MolangSlot.evaluate(left, out);
        }
    }

    @Override
    public IValue simplify() {
        this.left = left.simplify();
        this.right = right.simplify();

        return left instanceof MolangDefinable ? this : left;
    }

    @Override
    public String toString() {
        return left + " ?? " + right;
    }
}
