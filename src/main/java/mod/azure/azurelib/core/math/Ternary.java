/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

import javax.annotation.Nonnull;

/**
 * Ternary operator class This value implementation allows to return different values depending on given condition value
 */
public final class Ternary implements IValue {

    private final IValue condition;

    private final IValue ifTrue;

    private final IValue ifFalse;

    public Ternary(IValue condition, IValue ifTrue, IValue ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    public IValue condition() {
        return this.condition;
    }

    public IValue ifTrue() {
        return this.ifTrue;
    }

    public IValue ifFalse() {
        return this.ifFalse;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Ternary))
            return false;
        Ternary other = (Ternary) o;
        return java.util.Objects.equals(this.condition, other.condition)
            && java.util.Objects.equals(this.ifTrue, other.ifTrue)
            && java.util.Objects.equals(this.ifFalse, other.ifFalse);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + java.util.Objects.hashCode(this.condition);
        result = 31 * result + java.util.Objects.hashCode(this.ifTrue);
        result = 31 * result + java.util.Objects.hashCode(this.ifFalse);
        return result;
    }

    @Override
    public double get() {
        return this.condition.get() != 0 ? this.ifTrue.get() : this.ifFalse.get();
    }

    @Override
    public IValue simplify() {
        IValue condition = this.condition.simplify();

        if (condition instanceof Constant)
            return condition.get() != 0 ? this.ifTrue.simplify() : this.ifFalse.simplify();

        return new Ternary(condition, this.ifTrue.simplify(), this.ifFalse.simplify());
    }

    @Override
    public @Nonnull String toString() {
        return this.condition.toString() + " ? " + this.ifTrue.toString() + " : " + this.ifFalse.toString();
    }
}
