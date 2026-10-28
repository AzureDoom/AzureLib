package mod.azure.azurelib.core.molang;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

/**
 * An immutable Molang array: the value type of {@code array.*} references and of queries that return collections, such
 * as {@code query.get_nearby_entities}. Elements are numbers (stored boxed), strings or entity references.
 * <p>
 * Molang arrays are references: assigning one to a variable shares it rather than copying it, and two arrays are only
 * equal if they are the same array. Molang can't modify them, so sharing is always safe.
 * </p>
 * <p>
 * Indexing follows Bedrock: the index is floored, a negative index reads element 0 and an index past the end wraps
 * around ({@code index = max(0, index) % size}). Indexing an empty array gives 0.
 * </p>
 */
public final class MolangArray {

    public static final MolangArray EMPTY = new MolangArray(new Object[0]);

    private final Object[] elements;

    private MolangArray(Object[] elements) {
        this.elements = elements;
    }

    /**
     * @param elements numbers, strings or entity references; {@code null} elements are stored as 0
     */
    public static MolangArray of(Object... elements) {
        if (elements.length == 0) {
            return EMPTY;
        }

        var copy = Arrays.copyOf(elements, elements.length, Object[].class);

        for (int i = 0; i < copy.length; i++) {
            copy[i] = normalize(copy[i]);
        }

        return new MolangArray(copy);
    }

    public static MolangArray of(Collection<?> elements) {
        return elements.isEmpty() ? EMPTY : of(elements.toArray());
    }

    public static MolangArray ofNumbers(double... values) {
        if (values.length == 0) {
            return EMPTY;
        }

        var boxed = new Object[values.length];

        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }

        return new MolangArray(boxed);
    }

    /**
     * Wraps {@code elements} without copying. The caller must not modify the array afterwards.
     */
    public static MolangArray wrap(Object[] elements) {
        if (elements.length == 0) {
            return EMPTY;
        }

        for (int i = 0; i < elements.length; i++) {
            elements[i] = normalize(elements[i]);
        }

        return new MolangArray(elements);
    }

    /**
     * Bedrock's array index rule: floor, negatives clamp to 0, past-the-end wraps.
     *
     * @param size must be greater than 0
     */
    public static int wrapIndex(double index, int size) {
        if (!(index > 0)) {
            // Negative, zero and NaN all read the first element.
            return 0;
        }

        var floored = Math.floor(index);
        return floored >= Integer.MAX_VALUE ? (int) (floored % size) : ((int) floored) % size;
    }

    public int size() {
        return elements.length;
    }

    public boolean isEmpty() {
        return elements.length == 0;
    }

    /**
     * @return the element at a raw (already wrapped) index: a {@link Double}, a {@link String} or an entity reference
     */
    public Object get(int index) {
        return elements[index];
    }

    /**
     * @return the element for a Molang index, using Bedrock's wrapping rules, or 0 if the array is empty
     */
    public Object getWrapped(double index) {
        return elements.length == 0 ? 0D : elements[wrapIndex(index, elements.length)];
    }

    private static Object normalize(Object element) {
        if (element == null) {
            return 0D;
        }

        if (element instanceof Number number && !(element instanceof Double)) {
            return number.doubleValue();
        }

        return element;
    }

    @Override
    public String toString() {
        var joiner = new StringJoiner(", ", "[", "]");

        for (var element : elements) {
            joiner.add(element instanceof String string ? "'" + string + "'" : String.valueOf(element));
        }

        return joiner.toString();
    }
}
