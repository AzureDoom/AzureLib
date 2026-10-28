package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangQueryContext;

/**
 * {@code this}: the current value of whatever the expression is computing, supplied by the evaluation context through
 * {@link MolangQueryContext#thisValue()}. Its meaning depends on where the expression runs; in an AzureLib animation
 * keyframe it is the value the bone channel currently holds (see {@code AzMolangQueryContext}). Where nothing supplies
 * a value it is 0.
 */
public final class MolangThis implements IValue {

    public static final MolangThis INSTANCE = new MolangThis();

    private MolangThis() {}

    @Override
    public double get() {
        return MolangQueryContext.current().thisValue();
    }

    @Override
    public String toString() {
        return "this";
    }
}
