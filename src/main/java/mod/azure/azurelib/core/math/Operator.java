/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

/**
 * Operator class This class is responsible for performing a calculation of two values based on given operation.
 */
public class Operator implements IValue {

    public Operation operation;

    public IValue a;

    public IValue b;

    public Operator(Operation op, IValue a, IValue b) {
        this.operation = op;
        this.a = a;
        this.b = b;
    }

    @Override
    public double get() {
        return this.operation.calculate(this.a.get(), this.b.get());
    }

    /**
     * Folds constant operands, then drops operations that are exact no-ops for every input. Only identities that hold
     * bit-for-bit (including NaN, infinities and -0.0) are used, per the {@link IValue#simplify()} contract: so
     * {@code x * 1} and {@code x - 0} simplify, but {@code x + 0} (turns -0.0 into 0.0) and {@code x * 0} (NaN,
     * infinity) do not. Chains like {@code x * 2 * 3} are left alone too, since regrouping them changes rounding.
     */
    @Override
    public IValue simplify() {
        this.a = this.a.simplify();
        this.b = this.b.simplify();

        if (this.a instanceof Constant && this.b instanceof Constant) {
            return Constant.of(this.operation.calculate(this.a.get(), this.b.get()));
        }

        switch ((this.operation)) {
            case MUL:
                return isExactly(this.b, 1) ? this.a : isExactly(this.a, 1) ? this.b : this;
            case DIV:
            case POW:
                return isExactly(this.b, 1) ? this.a : this;
            case SUB:
                return isExactly(this.b, 0) && !isNegativeZero(this.b) ? this.a : this;
            default:
                return this;
        }
    }

    private static boolean isExactly(IValue value, double expected) {
        return value instanceof Constant && value.get() == expected;
    }

    private static boolean isNegativeZero(IValue value) {
        return Double.doubleToRawLongBits(value.get()) == Double.doubleToRawLongBits(-0.0);
    }

    @Override
    public String toString() {
        return a.toString() + " " + this.operation.sign + " " + b.toString();
    }
}
