package mod.azure.azurelib.render;

/**
 * Allocation-free ordering helpers for {@link AzTranslucencySorter}.
 */
final class AzQuadOrdering {

    private AzQuadOrdering() {}

    /**
     * Sorts {@code indices[0, count)} so their {@code keys} descend (farthest first). Stable: indices with equal keys
     * keep their order, so a later pass drawn over the same surface stays on top, the way it is without sorting. A
     * bottom-up merge sort that uses {@code scratch} (at least {@code count} long) instead of allocating.
     */
    static void sortDescendingStable(int[] indices, float[] keys, int count, int[] scratch) {
        if (count < 2) {
            return;
        }

        int[] source = indices;
        int[] target = scratch;

        for (int width = 1; width < count; width *= 2) {
            for (int low = 0; low < count; low += width * 2) {
                int middle = Math.min(low + width, count);
                int high = Math.min(low + width * 2, count);
                merge(source, target, keys, low, middle, high);
            }

            int[] swap = source;
            source = target;
            target = swap;
        }

        if (source != indices) {
            System.arraycopy(source, 0, indices, 0, count);
        }
    }

    private static void merge(int[] source, int[] target, float[] keys, int low, int middle, int high) {
        int left = low, right = middle, out = low;

        while (left < middle && right < high) {
            // Take from the right only when strictly farther, so ties keep submission order.
            if (key(keys, source[right]) > key(keys, source[left])) {
                target[out++] = source[right++];
            } else {
                target[out++] = source[left++];
            }
        }

        while (left < middle) {
            target[out++] = source[left++];
        }

        while (right < high) {
            target[out++] = source[right++];
        }
    }

    private static float key(float[] keys, int index) {
        float key = keys[index];
        return Float.isNaN(key) ? 0 : key;
    }
}
