/**
 * This class is a fork of the matching class found in the SmartBrainLib repository. Original source:
 * https://github.com/Tslat/SmartBrainLib Copyright © 2024 Tslat. Licensed under Mozilla Public License 2.0:
 * https://github.com/Tslat/SmartBrainLib/blob/1.21/LICENSE.
 */
package mod.azure.azurelib.sblforked.object;

import java.util.Objects;

/**
 * Triple-argument variant of {@link java.util.function.Predicate}
 */
@FunctionalInterface
public interface TriPredicate<A, B, C> {

    boolean test(A a, B b, C c);

    default TriPredicate<A, B, C> and(TriPredicate<? super A, ? super B, ? super C> other) {
        Objects.requireNonNull(other);

        return (A a, B b, C c) -> test(a, b, c) && other.test(a, b, c);
    }

    default TriPredicate<A, B, C> negate() {
        return (A a, B b, C c) -> !test(a, b, c);
    }

    default TriPredicate<A, B, C> or(TriPredicate<? super A, ? super B, ? super C> other) {
        Objects.requireNonNull(other);

        return (A a, B b, C c) -> test(a, b, c) || other.test(a, b, c);
    }
}
