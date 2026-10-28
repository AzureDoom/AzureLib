package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangArray;

/**
 * {@code array[index]}: reads one element of a {@link MolangArray} using Bedrock's rules (the index is floored,
 * negative reads element 0, past-the-end wraps around). Indexing anything that isn't an array, or an empty array, gives
 * 0. Arrays are read-only, so an index can't be assigned to.
 */
public final class MolangIndex implements MolangObjectValue {

    private IValue target;

    private IValue index;

    private final MolangSlot targetSlot = new MolangSlot();

    private final MolangSlot result = new MolangSlot();

    public MolangIndex(IValue target, IValue index) {
        this.target = target;
        this.index = index;
    }

    @Override
    public void evaluate(MolangSlot out) {
        MolangSlot.evaluate(target, targetSlot);
        MolangArray array = targetSlot.array();
        targetSlot.clear();

        if (array == null || array.isEmpty()) {
            // Still evaluate the index, so any side effects in it happen consistently.
            index.get();
            out.setNumber(0);
            return;
        }

        out.setObject(array.getWrapped(index.get()));
    }

    @Override
    public double get() {
        evaluate(result);
        double value = result.number();
        result.clear();
        return value;
    }

    @Override
    public IValue simplify() {
        this.target = target.simplify();
        this.index = index.simplify();
        return this;
    }

    @Override
    public String toString() {
        return target + "[" + index + "]";
    }
}
