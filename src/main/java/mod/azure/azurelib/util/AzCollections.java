package mod.azure.azurelib.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Small JDK-only replacements for the Guava immutable-collection factories used on other branches.
 */
public final class AzCollections {

    private AzCollections() {
        throw new UnsupportedOperationException();
    }

    /** Unmodifiable, null-rejecting copy of a list (Java 8 stand-in for {@code List.copyOf}). */
    public static <T> List<T> immutableList(Collection<? extends T> values) {
        List<T> copy = new ArrayList<>(values.size());
        for (T value : values) {
            copy.add(Objects.requireNonNull(value));
        }
        return Collections.unmodifiableList(copy);
    }

    /** Insertion-ordered, de-duplicated, unmodifiable copy (like Guava's {@code ImmutableSet.copyOf}). */
    public static <T> Set<T> immutableSet(Collection<? extends T> values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    /** Insertion-ordered, de-duplicated, unmodifiable set (like Guava's {@code ImmutableSet.of}). */
    @SafeVarargs
    public static <T> Set<T> immutableSetOf(T... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }
}
