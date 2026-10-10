/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.molang.expressions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.LazyVariable;

/**
 * An extension of the {@link MolangValue} class, allowing for compound expressions.
 */
public class MolangCompoundValue extends MolangValue {

    public final List<MolangValue> values = new ArrayList<>();

    public final Map<String, LazyVariable> locals = new HashMap<>();

    /**
     * The statement's expression tree when this compound holds exactly one plain statement (no assignment), so
     * {@link #get()} can evaluate it directly instead of iterating {@link #values}. Set by {@link #compact()}.
     */
    private IValue direct;

    public MolangCompoundValue(MolangValue baseValue) {
        super(baseValue);

        this.values.add(baseValue);
    }

    /**
     * Enables the single-statement fast path. Call once after all statements have been added; the parser does this. If
     * statements are added afterward, call it again.
     */
    public void compact() {
        this.direct = this.values.size() == 1 && this.values.get(0).getClass() == MolangValue.class
            ? this.values.get(0).getValueHolder()
            : null;
    }

    @Override
    public double get() {
        IValue direct = this.direct;

        if (direct != null)
            return direct.get();

        double value = 0;

        for (MolangValue molangValue : this.values) {
            value = molangValue.get();

            // A return statement ends the expression; later statements must not run.
            if (molangValue.isReturnValue())
                return value;
        }

        return value;
    }

    @Override
    public String toString() {
        StringJoiner builder = new StringJoiner("; ");

        for (MolangValue molangValue : this.values) {
            builder.add(molangValue.toString());

            if (molangValue.isReturnValue())
                break;
        }

        return builder.toString();
    }
}
