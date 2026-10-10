package mod.azure.azurelib.util;

/**
 * Immutable pair of ints. Minecraft 1.12.2 ships fastutil 7, which predates fastutil's {@code IntIntPair}.
 */
public final class IntIntPair {

    private final int first;

    private final int second;

    private IntIntPair(int first, int second) {
        this.first = first;
        this.second = second;
    }

    public static IntIntPair of(int first, int second) {
        return new IntIntPair(first, second);
    }

    public int firstInt() {
        return this.first;
    }

    public int secondInt() {
        return this.second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IntIntPair))
            return false;
        IntIntPair other = (IntIntPair) o;
        return this.first == other.first && this.second == other.second;
    }

    @Override
    public int hashCode() {
        return 31 * this.first + this.second;
    }

    @Override
    public String toString() {
        return "<" + this.first + "," + this.second + ">";
    }
}
