package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.MolangArray;
import mod.azure.azurelib.core.molang.expressions.MolangObjectVariable;

/**
 * {@code for_each(variable, array, expression)}: assigns each element of {@code array} to {@code variable} in turn and
 * evaluates {@code expression} for it. The variable is a {@code temp.*} or {@code variable.*} and gets the element with
 * normal assignment semantics, so it can hold the entity references {@code query.get_nearby_entities} returns:
 *
 * <pre>
 * {@code
 * t.count = 0;
 * for_each(t.pig, q.get_nearby_entities(8, 'minecraft:pig'), {
 *     t.count = t.count + (t.pig->q.is_baby);
 * });
 * return t.count;
 * }
 * </pre>
 * <p>
 * {@code break} leaves the loop and {@code continue} skips to the next element, as in {@link MolangLoop}. Anything that
 * isn't an array iterates zero times. At most {@value MolangLoop#MAX_ITERATIONS} elements are visited. After the loop
 * the variable keeps the last element it was given. Its own value is 0.
 * </p>
 */
public final class MolangForEach implements IValue {

    private final MolangObjectVariable variable;

    private IValue array;

    private IValue body;

    private final MolangFrame frame;

    private final MolangSlot arraySlot = new MolangSlot();

    public MolangForEach(MolangObjectVariable variable, IValue array, IValue body, MolangFrame frame) {
        this.variable = variable;
        this.array = array;
        this.body = body;
        this.frame = frame;
    }

    @Override
    public double get() {
        MolangSlot.evaluate(array, arraySlot);
        MolangArray elements = arraySlot.array();
        arraySlot.clear();

        if (elements == null) {
            return 0;
        }

        int count = Math.min(elements.size(), MolangLoop.MAX_ITERATIONS);

        for (int i = 0; i < count; i++) {
            variable.setObject(elements.get(i));
            body.get();

            if (frame.signal == MolangFrame.BREAK) {
                frame.signal = MolangFrame.NONE;
                break;
            }

            if (frame.signal == MolangFrame.CONTINUE) {
                frame.signal = MolangFrame.NONE;
                continue;
            }

            if (frame.signal == MolangFrame.RETURN) {
                break;
            }
        }

        return 0;
    }

    @Override
    public IValue simplify() {
        this.array = array.simplify();
        this.body = body.simplify();
        return this;
    }

    @Override
    public String toString() {
        return "for_each(" + variable + ", " + array + ", " + body + ")";
    }
}
