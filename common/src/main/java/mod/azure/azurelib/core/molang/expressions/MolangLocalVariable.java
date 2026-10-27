package mod.azure.azurelib.core.molang.expressions;

import mod.azure.azurelib.core.molang.LazyVariable;

/**
 * An expression-scoped Molang variable: {@code temp.*} ({@code t.*}), or an unprefixed name assigned inside the
 * expression. Every evaluation of the expression starts with it unset at 0, so a value never leaks from one entity's
 * evaluation into the next.
 */
public final class MolangLocalVariable extends LazyVariable implements MolangDefinable {

    private boolean defined;

    public MolangLocalVariable(String name) {
        super(name, 0);
        // LazyVariable's constructor goes through set(double), which marks the variable as defined.
        this.defined = false;
    }

    @Override
    public void set(double value) {
        super.set(value);
        this.defined = true;
    }

    /**
     * Back to 0 and unset; called at the start of each evaluation of the owning expression.
     */
    public void reset() {
        super.set(0);
        this.defined = false;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }
}
