package mod.azure.azurelib.core.molang.runtime;

import mod.azure.azurelib.core.math.IValue;

/**
 * {@code return value}, {@code break} and {@code continue}. They raise a signal on the expression's
 * {@link MolangFrame}; blocks, loops and the enclosing program stop running statements when they see it.
 */
public final class MolangControlFlow implements IValue {

    private final int signal;

    private IValue value;

    private final MolangFrame frame;

    private MolangControlFlow(int signal, IValue value, MolangFrame frame) {
        this.signal = signal;
        this.value = value;
        this.frame = frame;
    }

    public static MolangControlFlow returning(IValue value, MolangFrame frame) {
        return new MolangControlFlow(MolangFrame.RETURN, value, frame);
    }

    public static MolangControlFlow breaking(MolangFrame frame) {
        return new MolangControlFlow(MolangFrame.BREAK, null, frame);
    }

    public static MolangControlFlow continuing(MolangFrame frame) {
        return new MolangControlFlow(MolangFrame.CONTINUE, null, frame);
    }

    @Override
    public double get() {
        double result = value == null ? 0 : value.get();

        // Don't let a nested signal (e.g. a return inside the returned expression) be overwritten.
        if (frame.signal == MolangFrame.NONE) {
            frame.signal = signal;
            frame.returnValue = result;
        }

        return result;
    }

    @Override
    public IValue simplify() {
        if (value != null) {
            this.value = value.simplify();
        }

        return this;
    }

    @Override
    public String toString() {
        if (frame.signal == MolangFrame.RETURN) {
            return "return " + value;
        } else if (frame.signal == MolangFrame.BREAK) {
            return "break";
        } else {
            return "continue";
        }
    }
}
