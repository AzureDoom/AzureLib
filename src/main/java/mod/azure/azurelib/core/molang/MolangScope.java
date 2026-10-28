package mod.azure.azurelib.core.molang;

import java.lang.ref.WeakReference;
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
 * <p>
 * Besides numbers, a variable can hold a string, a {@link MolangArray} or an entity reference
 * ({@code v.mode = 'angry'}, {@code v.nearby = q.get_nearby_entities(8)}, or {@link #setObject(String, Object)} from
 * code). Entity references are held weakly: a scope never keeps an entity alive, and once the entity has been collected
 * the variable reads as unset, so {@code v.target ?? ...} behaves like Bedrock's stale actor references.
 * </p>
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
     * Non-numeric values by slot: a {@link String}, a {@link MolangArray}, or a {@link WeakReference} to anything else
     * (entity references). Allocated the first time this scope stores one, so numeric-only scopes pay nothing.
     */
    private Object[] objects;

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
        if (slot >= assigned.length || !assigned[slot]) {
            return false;
        }

        // An entity reference whose entity is gone reads as unset.
        return objects == null
            || !(objects[slot] instanceof WeakReference)
            || ((WeakReference<?>) objects[slot]).get() != null;
    }

    public void set(int slot, double value) {
        ensureCapacity(slot);
        values[slot] = value;
        assigned[slot] = true;

        if (objects != null) {
            objects[slot] = null;
        }
    }

    /**
     * Stores a string, array or entity reference in the slot. Numbers and {@code null} are stored as numbers.
     */
    public void setObject(int slot, Object value) {
        if (value == null || value instanceof Number) {
            set(slot, value == null ? 0 : ((Number) value).doubleValue());
            return;
        }

        ensureCapacity(slot);

        if (objects == null) {
            objects = new Object[values.length];
        }

        Object existing = objects[slot];

        if (existing instanceof WeakReference<?> && ((WeakReference<?>) existing).get() == value) {
            // Re-assigning the same entity every frame shouldn't allocate a new reference each time.
        } else if (value instanceof String || value instanceof MolangArray) {
            objects[slot] = value;
        } else {
            objects[slot] = new WeakReference<>(value);
        }

        values[slot] = 0;
        assigned[slot] = true;
    }

    /**
     * @return the string, array or entity reference in the slot, or {@code null} if it holds a number, is unset, or
     *         held an entity that has since been collected
     */
    public Object getObject(int slot) {
        if (objects == null || slot >= objects.length || !assigned[slot]) {
            return null;
        }

        Object object = objects[slot];

        if (object instanceof WeakReference<?>) {
            return ((WeakReference<?>) object).get();
        }

        return object;
    }

    public void remove(int slot) {
        if (slot < assigned.length) {
            assigned[slot] = false;
            values[slot] = 0;

            if (objects != null) {
                objects[slot] = null;
            }
        }
    }

    private void ensureCapacity(int slot) {
        if (slot >= values.length) {
            int size = Math.max(slot + 1, values.length * 2);
            values = Arrays.copyOf(values, size);
            assigned = Arrays.copyOf(assigned, size);

            if (objects != null) {
                objects = Arrays.copyOf(objects, size);
            }
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
     * Stores a string, {@link MolangArray} or entity reference, e.g. {@code setObject("v.mode", "angry")}.
     *
     * @param name {@code "v.foo"}, {@code "variable.foo"} or just {@code "foo"}
     */
    public void setObject(String name, Object value) {
        setObject(slot(variableName(name)), value);
    }

    /**
     * @param name {@code "v.foo"}, {@code "variable.foo"} or just {@code "foo"}
     * @return the string, array or entity reference this scope holds for the variable, or {@code null}
     */
    public Object getObject(String name) {
        Integer slot = SLOTS.get(variableName(name));
        return slot == null ? null : getObject(slot);
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

        if (objects != null) {
            Arrays.fill(objects, null);
        }
    }

    /**
     * @return every numeric variable this scope holds, by full name, for debugging. Variables holding strings, arrays
     *         or entities are reported by {@link #objectSnapshot()} instead.
     */
    public Map<String, Double> snapshot() {
        LinkedHashMap<String, Double> snapshot = new LinkedHashMap<>();

        synchronized (SLOT_NAMES) {
            for (int slot = 0; slot < assigned.length && slot < SLOT_NAMES.size(); slot++) {
                if (assigned[slot] && (objects == null || objects[slot] == null)) {
                    snapshot.put(SLOT_NAMES.get(slot), values[slot]);
                }
            }
        }

        return snapshot;
    }

    /**
     * @return every variable this scope holds a string, array or live entity reference for, by full name, for debugging
     */
    public Map<String, Object> objectSnapshot() {
        LinkedHashMap<String, Object> snapshot = new LinkedHashMap<>();

        if (objects == null) {
            return snapshot;
        }

        synchronized (SLOT_NAMES) {
            for (int slot = 0; slot < objects.length && slot < SLOT_NAMES.size(); slot++) {
                Object object = getObject(slot);

                if (object != null) {
                    snapshot.put(SLOT_NAMES.get(slot), object);
                }
            }
        }

        return snapshot;
    }

    @Override
    public String toString() {
        Map<String, Object> objectValues = objectSnapshot();
        return "MolangScope" + snapshot() + (objectValues.isEmpty() ? "" : objectValues);
    }
}
