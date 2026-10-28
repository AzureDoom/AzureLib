package mod.azure.azurelib.core.molang.expressions;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.core.molang.runtime.MolangObjectValue;

/**
 * A variable that can hold a string, array or entity reference as well as a number: {@code temp.*} and
 * {@code variable.*}. Assigning a number clears any object it held, and vice versa. Read as a number, a variable
 * holding an object is 0.
 */
public interface MolangObjectVariable extends MolangObjectValue {

    void set(double value);

    /**
     * Stores {@code value}; boxed numbers are stored as numbers and {@code null} as 0.
     */
    void setObject(@Nullable Object value);
}
