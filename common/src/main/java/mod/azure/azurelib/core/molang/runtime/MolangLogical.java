package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.Constant;
import mod.azure.azurelib.core.math.IValue;

/**
 * Short-circuiting {@code &&} and {@code ||}: the right side is only evaluated when it can change the result, so
 * {@code q.is_on_ground && (v.landed = 1)} only assigns while on the ground. Evaluates to 1 or 0.
 */
public final class MolangLogical implements IValue {

    private final boolean isAnd;

    private IValue left;

    private IValue right;

    private MolangLogical(boolean isAnd, IValue left, IValue right) {
        this.isAnd = isAnd;
        this.left = left;
        this.right = right;
    }

    public static MolangLogical and(IValue left, IValue right) {
        return new MolangLogical(true, left, right);
    }

    public static MolangLogical or(IValue left, IValue right) {
        return new MolangLogical(false, left, right);
    }

    @Override
    public double get() {
        if (isAnd) {
            return left.get() != 0 && right.get() != 0 ? 1 : 0;
        }

        return left.get() != 0 || right.get() != 0 ? 1 : 0;
    }

    @Override
    public IValue simplify() {
        this.left = left.simplify();
        this.right = right.simplify();

        if (left instanceof Constant && right instanceof Constant) {
            return Constant.of(get());
        }

        return this;
    }

    @Override
    public String toString() {
        return left + (isAnd ? " && " : " || ") + right;
    }
}
