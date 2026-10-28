package mod.azure.azurelib.core.molang.expressions;

import mod.azure.azurelib.core.molang.LazyVariable;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * An expression-scoped Molang variable: {@code temp.*} ({@code t.*}), or an unprefixed name assigned inside the
 * expression. Every evaluation of the expression starts with it unset at 0, so a value never leaks from one entity's
 * evaluation into the next. It can hold a string, array or entity reference as well as a number; the owning expression
 * drops any such reference when it finishes, so cached expression trees never keep an entity alive.
 */
public final class MolangLocalVariable extends LazyVariable implements MolangDefinable, MolangObjectVariable {

    private boolean defined;

    private Object object;

    public MolangLocalVariable(String name) {
        super(name, 0);
        // LazyVariable's constructor goes through set(double), which marks the variable as defined.
        this.defined = false;
    }

    @Override
    public void set(double value) {
        super.set(value);
        this.object = null;
        this.defined = true;
    }

    @Override
    public void setObject(Object value) {
        if (value == null || value instanceof Number) {
            set(value == null ? 0 : ((Number) value).doubleValue());
            return;
        }

        super.set(0);
        this.object = value;
        this.defined = true;
    }

    @Override
    public void evaluate(MolangSlot out) {
        if (object != null) {
            out.setObject(object);
        } else {
            out.setNumber(get());
        }
    }

    /**
     * @return the string, array or entity reference held, or {@code null} if the variable holds a number
     */
    public Object getObject() {
        return object;
    }

    /**
     * Back to 0 and unset; called at the start of each evaluation of the owning expression.
     */
    public void reset() {
        super.set(0);
        this.object = null;
        this.defined = false;
    }

    /**
     * Drops an object reference without changing anything else; called when the owning expression finishes.
     */
    public void releaseObject() {
        this.object = null;
    }

    @Override
    public boolean isDefined() {
        return defined;
    }
}
