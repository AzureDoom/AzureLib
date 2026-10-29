package mod.azure.azurelib.animation.pose;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The pose modifiers of one animator, in run order. Filled in {@code AzAnimator#registerPoseModifiers}; addons and
 * compat code may also add or remove modifiers later through {@code AzAnimator#getPoseModifiers()}.
 *
 * @param <T> the animatable type
 */
public final class AzPoseModifierRegistry<T> {

    private static final Comparator<AzPoseModifierEntry<?>> RUN_ORDER = Comparator
        .<AzPoseModifierEntry<?>>comparingInt(AzPoseModifierEntry::priority)
        .thenComparingInt(AzPoseModifierEntry::order);

    @SuppressWarnings("rawtypes")
    private static final AzPoseModifierEntry[] NO_ENTRIES = new AzPoseModifierEntry[0];

    private final Map<String, AzPoseModifierEntry<T>> entriesById = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    private AzPoseModifierEntry<T>[] sorted = NO_ENTRIES;

    private boolean dirty;

    private int version;

    private int nextOrder;

    public AzPoseModifierEntry<T> add(String id, AzPoseModifier<T> modifier) {
        if (entriesById.containsKey(id)) {
            throw new IllegalArgumentException("A pose modifier with id '" + id + "' is already registered");
        }

        AzPoseModifierEntry<T> entry = new AzPoseModifierEntry<>(this, id, modifier, nextOrder++);
        entriesById.put(id, entry);
        markDirty();
        return entry;
    }

    public boolean remove(String id) {
        boolean removed = entriesById.remove(id) != null;

        if (removed) {
            markDirty();
        }

        return removed;
    }

    public @Nullable AzPoseModifierEntry<T> get(String id) {
        return entriesById.get(id);
    }

    public boolean isEmpty() {
        return entriesById.isEmpty();
    }

    public int version() {
        resortIfDirty();
        return version;
    }

    AzPoseModifierEntry<T>[] sorted() {
        resortIfDirty();
        return sorted;
    }

    void markDirty() {
        dirty = true;
    }

    @SuppressWarnings("unchecked")
    private void resortIfDirty() {
        if (!dirty) {
            return;
        }

        ArrayList<AzPoseModifierEntry<T>> list = new ArrayList<>(entriesById.values());
        list.sort(RUN_ORDER);
        sorted = list.toArray(NO_ENTRIES);
        version++;
        dirty = false;
    }
}
