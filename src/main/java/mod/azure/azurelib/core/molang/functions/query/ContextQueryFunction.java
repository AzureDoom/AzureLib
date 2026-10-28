package mod.azure.azurelib.core.molang.functions.query;

import java.util.Locale;

import mod.azure.azurelib.core.math.IValue;
import mod.azure.azurelib.core.math.functions.Function;
import mod.azure.azurelib.core.molang.MolangQueryContext;
import mod.azure.azurelib.core.molang.runtime.MolangSlot;

/**
 * Base class for query functions that read game state through the current {@link MolangQueryContext}.
 * <p>
 * These live outside {@code mod.azure.azurelib.core.molang.functions} on purpose: functions in that package are folded
 * to a constant at parse time when all their arguments are constant, which would freeze a call like
 * {@code query.position(1)} to whatever it returned while the animation was loading.
 * </p>
 */
public abstract class ContextQueryFunction extends Function {

    /** Main hand, as returned by {@link #handArg}. */
    public static final int MAIN_HAND = 0;

    /** Offhand, as returned by {@link #handArg}. */
    public static final int OFF_HAND = 1;

    /** Scratch slot for reading string and object arguments; one per call site, so evaluation allocates nothing. */
    private final MolangSlot argSlot = new MolangSlot();

    protected ContextQueryFunction(IValue[] values, String name) throws Exception {
        super(values, name);
    }

    @Override
    public final double get() {
        return evaluate(MolangQueryContext.current());
    }

    protected abstract double evaluate(MolangQueryContext context);

    /** The argument at {@code index} as an integer (axis, slot, hand). */
    protected int intArg(int index) {
        return (int) this.getArg(index);
    }

    /**
     * The argument at {@code index} as a string, or {@code null} if it is missing or not a string.
     */
    protected String stringArg(int index) {
        if (index < 0 || index >= this.args.length) {
            return null;
        }

        MolangSlot.evaluate(this.args[index], argSlot);
        String string = argSlot.string();
        argSlot.clear();
        return string;
    }

    /**
     * Evaluates the argument at {@code index} into {@code out}; a missing argument evaluates to 0. The caller must
     * {@link MolangSlot#clear() clear} {@code out} once done with an object it holds.
     */
    protected void argInto(int index, MolangSlot out) {
        if (index < 0 || index >= this.args.length) {
            out.setNumber(0);
        } else {
            MolangSlot.evaluate(this.args[index], out);
        }
    }

    /**
     * A hand argument, given either as a number (0 = main hand, 1 = offhand) or as a Bedrock name: {@code 'main_hand'}
     * / {@code 'off_hand'}, or the slot names {@code 'slot.weapon.mainhand'} / {@code 'slot.weapon.offhand'}.
     *
     * @return {@link #MAIN_HAND} or {@link #OFF_HAND}; {@code fallback} if the argument is missing; -1 for an
     *         unrecognized name
     */
    protected int handArg(int index, int fallback) {
        if (index < 0 || index >= this.args.length) {
            return fallback;
        }

        MolangSlot.evaluate(this.args[index], argSlot);
        String string = argSlot.string();
        double number = argSlot.number();
        argSlot.clear();

        if (string == null) {
            return (int) number == OFF_HAND ? OFF_HAND : MAIN_HAND;
        }

        return handFromName(string);
    }

    /**
     * @return {@link #MAIN_HAND}, {@link #OFF_HAND}, or -1 if {@code name} isn't a hand name
     */
    public static int handFromName(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "main_hand":
            case "mainhand":
            case "slot.weapon.mainhand":
                return MAIN_HAND;

            case "off_hand":
            case "offhand":
            case "slot.weapon.offhand":
                return OFF_HAND;

            default:
                return -1;
        }
    }
}
