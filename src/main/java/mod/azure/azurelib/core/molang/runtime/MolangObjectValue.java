package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;

/**
 * An expression node that can evaluate to something other than a number: a string, a
 * {@link mod.azure.azurelib.core.molang.MolangArray} or an entity reference.
 * <p>
 * {@link #get()} still works on these nodes and returns the numeric value, or 0 when the value isn't a number, so they
 * can be used anywhere a number is expected. Nodes that care about non-numeric values (string equality, assignment,
 * indexing, {@code for_each}, the arrow operator) call {@link #evaluate} instead. The compiler checks for this
 * interface to decide when it needs those object-aware nodes, so numeric-only expressions keep the plain fast path.
 * </p>
 */
public interface MolangObjectValue extends IValue {

    /**
     * Evaluates this node once, writing the result into {@code out}. Must have the same side effects as {@link #get()}
     * and write {@code out} as its last step.
     */
    void evaluate(MolangSlot out);
}
