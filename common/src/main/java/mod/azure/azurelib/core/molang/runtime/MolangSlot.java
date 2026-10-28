package mod.azure.azurelib.core.molang.runtime;

import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangArray;

/**
 * A reusable holder for one Molang runtime value: a number, or an object (a string, a {@link MolangArray} or an entity
 * reference). Nodes that need non-numeric operands own their slots, allocated once when the expression is compiled, so
 * evaluating strings, arrays and entity references allocates nothing and never boxes a number.
 * <p>
 * A node that reads an object out of a slot must {@link #clear()} it afterwards. Compiled trees are cached for the
 * lifetime of the resource reload, so a slot left holding an entity would keep that entity, and its level, alive.
 * </p>
 */
public final class MolangSlot {

    private double number;

    @Nullable
    private Object object;

    /**
     * Evaluates {@code value} into {@code out}: through {@link MolangObjectValue#evaluate} if it can produce objects,
     * otherwise as a plain number.
     */
    public static void evaluate(IValue value, MolangSlot out) {
        if (value instanceof MolangObjectValue objectValue) {
            objectValue.evaluate(out);
        } else {
            out.setNumber(value.get());
        }
    }

    public void setNumber(double value) {
        this.number = value;
        this.object = null;
    }

    /**
     * Stores {@code value}. Boxed numbers are stored as numbers and {@code null} as 0, so a slot only ever holds an
     * object for genuinely non-numeric values.
     */
    public void setObject(@Nullable Object value) {
        if (value == null) {
            setNumber(0);
        } else if (value instanceof Number boxed) {
            setNumber(boxed.doubleValue());
        } else {
            this.number = 0;
            this.object = value;
        }
    }

    /** Copies another slot's value into this one. */
    public void set(MolangSlot other) {
        this.number = other.number;
        this.object = other.object;
    }

    public boolean isObject() {
        return object != null;
    }

    /**
     * @return the number held, or 0 when the slot holds an object
     */
    public double number() {
        return number;
    }

    /**
     * @return the object held, or {@code null} when the slot holds a number
     */
    public @Nullable Object object() {
        return object;
    }

    /**
     * @return the string held, or {@code null}
     */
    public @Nullable String string() {
        return object instanceof String string ? string : null;
    }

    /**
     * @return the array held, or {@code null}
     */
    public @Nullable MolangArray array() {
        return object instanceof MolangArray array ? array : null;
    }

    /**
     * Drops any object reference. Call once the value has been consumed.
     */
    public void clear() {
        this.number = 0;
        this.object = null;
    }

    @Override
    public String toString() {
        return object == null ? Double.toString(number) : String.valueOf(object);
    }
}
