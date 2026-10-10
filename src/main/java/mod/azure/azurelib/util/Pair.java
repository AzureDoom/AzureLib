package mod.azure.azurelib.util;

import java.util.Objects;

/**
 * Immutable pair, standing in for DataFixerUpper's {@code com.mojang.datafixers.util.Pair} which does not exist on
 * 1.7.10.
 */
public final class Pair<F, S> {

    private final F first;

    private final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    public F getFirst() {
        return this.first;
    }

    public S getSecond() {
        return this.second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Pair))
            return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.first, pair.first) && Objects.equals(this.second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.first, this.second);
    }

    @Override
    public String toString() {
        return "(" + this.first + ", " + this.second + ")";
    }
}
