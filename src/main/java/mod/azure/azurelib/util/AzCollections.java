package mod.azure.azurelib.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Small JDK-only replacements for the Guava immutable-collection factories used on other branches.
 */
public final class AzCollections {

    private AzCollections() {
        throw new UnsupportedOperationException();
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
