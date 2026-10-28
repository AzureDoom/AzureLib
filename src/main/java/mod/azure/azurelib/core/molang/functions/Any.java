package mod.azure.azurelib.core.molang.functions;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.molang.runtime.MolangEquality;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * {@code query.any(value, a, b, ...)}: 1 if any argument after the first equals the first, else 0. Arguments may be
 * strings ({@code q.any(q.get_equipped_item_name, 'bow', 'crossbow')}), compared with Molang's {@code ==} rules.
 */
public class Any extends Function {

    private final MolangSlot value = new MolangSlot();

    private final MolangSlot candidate = new MolangSlot();

    public Any(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public int getRequiredArguments() {
        return 2;
    }

    @Override
    public double get() {
        MolangSlot.evaluate(this.args[0], value);
        int result = 0;

        for (int i = 1; i < this.args.length; i++) {
            MolangSlot.evaluate(this.args[i], candidate);
            boolean equal = MolangEquality.equal(value, candidate);
            candidate.clear();

            if (equal) {
                result = 1;
                break;
            }
        }

        value.clear();
        return result;
    }
}
