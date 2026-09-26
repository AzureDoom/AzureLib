/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

import org.jetbrains.annotations.NotNull;

/**
 * Ternary operator class This value implementation allows to return different values depending on given condition value
 */
public record Ternary(
    IValue condition,
    IValue ifTrue,
    IValue ifFalse
) implements IValue {

    @Override
    public double get() {
        return this.condition.get() != 0 ? this.ifTrue.get() : this.ifFalse.get();
    }

    @Override
    public IValue simplify() {
        var condition = this.condition.simplify();

        if (condition instanceof Constant)
            return condition.get() != 0 ? this.ifTrue.simplify() : this.ifFalse.simplify();

        return new Ternary(condition, this.ifTrue.simplify(), this.ifFalse.simplify());
    }

    @Override
    public @NotNull String toString() {
        return this.condition.toString() + " ? " + this.ifTrue.toString() + " : " + this.ifFalse.toString();
    }
}
