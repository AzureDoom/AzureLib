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
        return this.operation.calculate(a.get(), b.get());
    }

    @Override
    public String toString() {
        return a.toString() + " " + this.operation.sign + " " + b.toString();
    }
}
