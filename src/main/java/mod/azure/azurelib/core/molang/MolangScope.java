package mod.azure.azurelib.core.molang;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Storage for Molang {@code variable.*} (short form {@code v.*}) values belonging to one animatable, so each entity,
 * block entity or item keeps its own copy. A value assigned in one keyframe expression (e.g.
 * {@code v.charge = math.clamp(v.charge + 0.1, 0, 1)}) is readable from every other expression evaluated for the same
 * animatable, and survives from frame to frame until it is changed or {@link #clear() cleared}.
 * <p>
 * The animator binds the animatable's scope with {@link #setCurrent} for the duration of each animate call, the same
 * way it binds the query context. Code can read and write values too, which is the simplest way to drive an animation
 * from game logic without registering a global variable:
 *
 * <pre>
 * {@code
 *
 * animator.context().molangScope().set("v.attack_charge", charge);
 * }
 * </pre>
 * <p>
 * Reads of a variable this scope has never set fall back to a variable with the same name registered globally on
 * {@link MolangParser} (with {@code register} or driven with {@code setValue}/{@code setMemoizedValue}), and to 0
 * otherwise. Render thread only, like the rest of Molang evaluation.
 */
public final class MolangScope {

    /** Used when no animatable is bound, e.g. expressions evaluated outside an animate call. */
    public static final MolangScope GLOBAL = new MolangScope();

    private static final Map<String, Integer> SLOTS = new ConcurrentHashMap<>();

    private static final List<String> SLOT_NAMES = new ArrayList<>();

    private static MolangScope current = GLOBAL;

    private double[] values = new double[8];

    private boolean[] assigned = new boolean[8];

    /**
     * @return the scope Molang {@code variable.*} reads and writes right now
     */
    public static MolangScope current() {
        return current;
    }

    /**
     * Binds {@code scope} for Molang evaluation; {@code null} binds {@link #GLOBAL}.
     *
     * @return the scope that was bound before, so callers can restore it
     */
    public static MolangScope setCurrent(MolangScope scope) {
        MolangScope previous = current;
        current = scope == null ? GLOBAL : scope;
        return previous;
    }

    /**
     * The slot index for a normalized variable name ({@code variable.foo}). Slots are global and never reused, so every
     * scope stores the same name at the same index. Called at parse time, possibly from several threads.
     */
    static int slot(String normalizedName) {
        Integer existing = SLOTS.get(normalizedName);

        if (existing != null) {
            return existing;
        }

        synchronized (SLOT_NAMES) {
            existing = SLOTS.get(normalizedName);

            if (existing != null) {
                return existing;
            }

            int slot = SLOT_NAMES.size();
            SLOT_NAMES.add(normalizedName);
            SLOTS.put(normalizedName, slot);
            return slot;
        }
    }

    /**
     * Turns {@code "v.foo"}, {@code "variable.foo"} or {@code "foo"} into {@code "variable.foo"}.
     */
    static String variableName(String name) {
        String normalized = MolangParser.normalizeName(name.toLowerCase(Locale.ROOT));
        return normalized.startsWith(MolangParser.VARIABLE_PREFIX)
            ? normalized
            : MolangParser.VARIABLE_PREFIX + normalized;
    }

    public double get(int slot) {
        return slot < assigned.length && assigned[slot] ? values[slot] : 0;
    }

    public boolean isSet(int slot) {
        return slot < assigned.length && assigned[slot];
    }

    public void set(int slot, double value) {
        if (slot >= values.length) {
            int size = Math.max(slot + 1, values.length * 2);
            values = Arrays.copyOf(values, size);
            assigned = Arrays.copyOf(assigned, size);
        }

        values[slot] = value;
        assigned[slot] = true;
    }

    public void remove(int slot) {
        if (slot < assigned.length) {
            assigned[slot] = false;
            values[slot] = 0;
        }
    }

    /**
     * @param name {@code "v.foo"}, {@code "variable.foo"} or just {@code "foo"}
     * @return the value this scope holds for the variable, or 0 if it has none
     */
    public double get(String name) {
        Integer slot = SLOTS.get(variableName(name));
        return slot == null ? 0 : get(slot);
    }

    /**
     * @param name {@code "v.foo"}, {@code "variable.foo"} or just {@code "foo"}
     */
    public void set(String name, double value) {
        set(slot(variableName(name)), value);
    }

    /**
     * @param name {@code "v.foo"}, {@code "variable.foo"} or just {@code "foo"}
     * @return whether this scope holds a value for the variable
     */
    public boolean isSet(String name) {
        Integer slot = SLOTS.get(variableName(name));
        return slot != null && isSet(slot);
    }

    /**
     * Forgets the variable, so reads fall back to the global value (or 0) and {@code ??} treats it as unset.
     */
    public void remove(String name) {
        Integer slot = SLOTS.get(variableName(name));

        if (slot != null) {
            remove(slot);
        }
    }

    /**
     * Forgets every variable in this scope.
     */
    public void clear() {
        Arrays.fill(values, 0);
        Arrays.fill(assigned, false);
    }

    /**
     * @return every variable this scope holds, by full name, for debugging
     */
    public Map<String, Double> snapshot() {
        LinkedHashMap<String, Double> snapshot = new LinkedHashMap<>();

        synchronized (SLOT_NAMES) {
            for (int slot = 0; slot < assigned.length && slot < SLOT_NAMES.size(); slot++) {
                if (assigned[slot]) {
                    snapshot.put(SLOT_NAMES.get(slot), values[slot]);
                }
            }
        }

        return snapshot;
    }

    @Override
    public String toString() {
        return "MolangScope" + snapshot();
    }
}
