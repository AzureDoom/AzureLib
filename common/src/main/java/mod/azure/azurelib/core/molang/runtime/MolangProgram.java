package mod.azure.azurelib.core.molang.runtime;

import java.util.List;
import java.util.StringJoiner;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.molang.expressions.MolangLocalVariable;

/**
 * A complete Molang expression with more than one statement, expression-scoped variables or control flow. Statements
 * run in order; {@code return} ends the expression with its value. Without a {@code return}, the value is that of the
 * last statement (AzureLib has always done this; Bedrock would return 0).
 */
public final class MolangProgram implements IValue {

    private final IValue[] statements;

    private final MolangLocalVariable[] locals;

    private final MolangFrame frame;

    public MolangProgram(List<IValue> statements, List<MolangLocalVariable> locals, MolangFrame frame) {
        this.statements = statements.toArray(new IValue[0]);
        this.locals = locals.toArray(new MolangLocalVariable[0]);
        this.frame = frame;
    }

    @Override
    public double get() {
        for (var local : locals) {
            local.reset();
        }

        frame.signal = MolangFrame.NONE;
        var value = 0D;

        for (var statement : statements) {
            value = statement.get();

            if (frame.signal != MolangFrame.NONE) {
                break;
            }
        }

        if (frame.signal == MolangFrame.RETURN) {
            value = frame.returnValue;
        }

        // A break or continue outside a loop just ends the expression.
        frame.signal = MolangFrame.NONE;

        // Cached trees outlive the entities they were evaluated for; don't let a temp keep one alive.
        for (var local : locals) {
            local.releaseObject();
        }

        return value;
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
        var joiner = new StringJoiner("; ");

        for (var statement : statements) {
            joiner.add(statement.toString());
        }

        return joiner.toString();
    }
}
