package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.Operation;

/**
 * {@code ==} and {@code !=} when either side may be a string, array or entity reference. The compiler only uses this
 * where an operand can produce one (a string literal, a variable, an index, a query returning objects); purely numeric
 * comparisons stay on {@link mod.azure.azurelib.core.math.Operator}.
 * <p>
 * Rules: two numbers compare like the numeric operator (within 0.00001); two strings compare exactly, case-sensitively;
 * arrays and entities compare by identity; a number never equals a non-number.
 * </p>
 */
public final class MolangEquality implements IValue {

    private IValue left;

    private IValue right;

    private final boolean negated;

    private final MolangSlot leftSlot = new MolangSlot();

    private final MolangSlot rightSlot = new MolangSlot();

    public MolangEquality(IValue left, IValue right, boolean negated) {
        this.left = left;
        this.right = right;
        this.negated = negated;
    }

    /**
     * Molang equality of two evaluated values.
     */
    public static boolean equal(MolangSlot a, MolangSlot b) {
        var objectA = a.object();
        var objectB = b.object();

        if (objectA == null && objectB == null) {
            return Operation.equals(a.number(), b.number());
        }

        if (objectA == null || objectB == null) {
            return false;
        }

        // Strings by content; arrays and entities by identity (neither overrides equals).
        return objectA == objectB || objectA.equals(objectB);
    }

    @Override
    public double get() {
        MolangSlot.evaluate(left, leftSlot);
        MolangSlot.evaluate(right, rightSlot);

        var equal = equal(leftSlot, rightSlot);

        leftSlot.clear();
        rightSlot.clear();

        return equal != negated ? 1 : 0;
    }

    @Override
    public IValue simplify() {
        this.left = left.simplify();
        this.right = right.simplify();

        var leftFixed = left instanceof Constant || left instanceof MolangString;
        var rightFixed = right instanceof Constant || right instanceof MolangString;

        return leftFixed && rightFixed ? Constant.of(get()) : this;
    }

    @Override
    public String toString() {
        return left + (negated ? " != " : " == ") + right;
    }
}
