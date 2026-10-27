package mod.azure.azurelib.core.molang.runtime;

/**
 * Control-flow state of one compiled Molang expression while it is evaluated: whether a {@code return}, {@code break}
 * or {@code continue} is unwinding, and the returned value. Each expression tree has its own frame, shared by its
 * nodes, so evaluation allocates nothing. Like the rest of Molang evaluation, a tree is evaluated by one thread at a
 * time (the render thread).
 */
public final class MolangFrame {

    public static final int NONE = 0;

    public static final int RETURN = 1;

    public static final int BREAK = 2;

    public static final int CONTINUE = 3;

    int signal = NONE;

    double returnValue;

    public boolean isUnwinding() {
        return signal != NONE;
    }
}
