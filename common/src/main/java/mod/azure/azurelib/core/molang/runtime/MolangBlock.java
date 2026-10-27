package mod.azure.azurelib.core.molang.runtime;

import java.util.List;
import java.util.StringJoiner;

import mod.azure.azurelib.core.math.IValue;

/**
 * A brace-delimited list of statements, {@code { v.a = 1; v.b = 2; }}, as used in conditionals and loops. A block is a
 * statement, so its own value is 0; a {@code return} inside it ends the whole expression.
 */
public final class MolangBlock implements IValue {

    private final IValue[] statements;

    private final MolangFrame frame;

    public MolangBlock(List<IValue> statements, MolangFrame frame) {
        this.statements = statements.toArray(new IValue[0]);
        this.frame = frame;
    }

    @Override
    public double get() {
        for (var statement : statements) {
            statement.get();

            if (frame.signal != MolangFrame.NONE) {
                break;
            }
        }

        return 0;
    }

    @Override
    public IValue simplify() {
        for (int i = 0; i < statements.length; i++) {
            statements[i] = statements[i].simplify();
        }

        return this;
    }

    @Override
    public String toString() {
        var joiner = new StringJoiner("; ", "{", statements.length > 0 ? ";}" : "}");

        for (var statement : statements) {
            joiner.add(statement.toString());
        }

        return joiner.toString();
    }
}
