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

    /**
     * Same results as {@link Operation#calculate(double, double)}, but switching on the operation here avoids a virtual
     * call into the enum constant's own class for every arithmetic node evaluated.
     */
    @Override
    public double get() {
        double a = this.a.get();
        double b = this.b.get();

        return switch (this.operation) {
            case ADD -> a + b;
            case SUB -> a - b;
            case MUL -> a * b;
            case DIV -> a / (b == 0 ? 1 : b);
            case MOD -> a % b;
            case POW -> Math.pow(a, b);
            case AND -> a != 0 && b != 0 ? 1 : 0;
            case OR -> a != 0 || b != 0 ? 1 : 0;
            case LESS -> a < b ? 1 : 0;
            case LESS_THAN -> a <= b ? 1 : 0;
            case GREATER_THAN -> a >= b ? 1 : 0;
            case GREATER -> a > b ? 1 : 0;
            case EQUALS -> Operation.equals(a, b) ? 1 : 0;
            case NOT_EQUALS -> !Operation.equals(a, b) ? 1 : 0;
        };
    }

    @Override
    public IValue simplify() {
        this.a = this.a.simplify();
        this.b = this.b.simplify();

        return this.a instanceof Constant && this.b instanceof Constant
            ? new Constant(this.operation.calculate(this.a.get(), this.b.get()))
            : this;
    }

    @Override
    public String toString() {
        return a.toString() + " " + this.operation.sign + " " + b.toString();
    }
}
