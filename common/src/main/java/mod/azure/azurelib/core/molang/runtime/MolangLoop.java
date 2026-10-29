package mod.azure.azurelib.core.molang.runtime;

import net.minecraft.util.Mth;

import mod.azure.azurelib.core.math.IValue;

/**
 * {@code loop(count, expression)}: evaluates {@code expression} {@code count} times (rounded down, at most
 * {@value #MAX_ITERATIONS}, as in Bedrock). {@code break} leaves the loop early and {@code continue} skips to the next
 * iteration. Its own value is 0.
 */
public final class MolangLoop implements IValue {

    public static final int MAX_ITERATIONS = 1024;

    private IValue count;

    private IValue body;

    private final MolangFrame frame;

    public MolangLoop(IValue count, IValue body, MolangFrame frame) {
        this.count = count;
        this.body = body;
        this.frame = frame;
    }

    @Override
    public double get() {
        double raw = count.get();
        int iterations = Double.isNaN(raw) ? 0 : (int) Mth.clamp(Math.floor(raw), 0, MAX_ITERATIONS);

        for (int i = 0; i < iterations; i++) {
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
        this.count = count.simplify();
        this.body = body.simplify();
        return this;
    }

    @Override
    public String toString() {
        return "loop(" + count + ", " + body + ")";
    }
}
