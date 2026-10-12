/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang;

import java.util.function.DoubleSupplier;

import mod.azure.azurelib.core.math.Variable;

/**
 * Lazy override of Variable, to allow for deferred value calculation. <br>
 * Optimizes rendering as values are not touched until needed (if at all).
 * <p>
 * Memoization state lives on the variable itself rather than in a wrapper supplier, so re-binding a memoized value
 * every frame ({@link #setMemoized(DoubleSupplier)}) and setting a constant ({@link #set(double)}) allocate nothing.
 */
public class LazyVariable extends Variable {

    private DoubleSupplier valueSupplier;

    /** When true, {@link #get()} returns {@link #cachedValue} once it has been computed. */
    private boolean memoized;

    private boolean computed;

    private double cachedValue;

    public LazyVariable(String name, double value) {
        super(name, 0);

        set(value);
    }

    public LazyVariable(String name, DoubleSupplier valueSupplier) {
        super(name, 0);

        set(valueSupplier);
    }

    /**
     * Instantiates a copy of this variable from this variable's current value and name
     */
    public static LazyVariable from(Variable variable) {
        return new LazyVariable(variable.getName(), variable.get());
    }

    /**
     * Set the new value for the variable, acting as a constant
     */
    @Override
    public void set(double value) {
        this.valueSupplier = null;
        this.cachedValue = value;
        this.memoized = true;
        this.computed = true;
    }

    /**
     * Set the new value supplier for the variable, evaluated on every {@link #get()}
     */
    public void set(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.memoized = false;
        this.computed = false;
    }

    /**
     * Set a value supplier that is evaluated at most once, on the first {@link #get()} after this call. Calling this
     * again resets the memoized value.
     */
    public void setMemoized(DoubleSupplier valueSupplier) {
        this.valueSupplier = valueSupplier;
        this.memoized = true;
        this.computed = false;
    }

    /**
     * Get the current value of the variable
     */
    @Override
    public double get() {
        if (this.memoized) {
            if (!this.computed) {
                this.cachedValue = this.valueSupplier.getAsDouble();
                this.computed = true;
            }

            return this.cachedValue;
        }

        return this.valueSupplier.getAsDouble();
    }
}
