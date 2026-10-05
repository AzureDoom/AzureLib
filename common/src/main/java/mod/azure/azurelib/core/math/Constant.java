/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.core.math;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constant class This class simply returns supplied in the constructor value
 * <p>
 * Parsers should create constants through {@link #of(double)}, which shares one instance per value. Animation files
 * repeat the same few numbers (0, 1, 90, ...) across thousands of keyframes, so this keeps one object per distinct
 * value instead of one per occurrence.
 */
@SuppressWarnings("unused")
public class Constant implements IValue {

    /**
     * Upper bound on distinct pooled values. Past it, {@link #of(double)} hands out unshared constants rather than
     * growing without limit (e.g. a resource pack with many unique generated values).
     */
    private static final int MAX_POOL_SIZE = 16_384;

    /**
     * Keyed by {@link Double}, whose equality is bit-exact: 0.0 and -0.0 stay distinct and NaN matches NaN, so a pooled
     * constant always returns exactly the value that was asked for. Concurrent because animation files are parsed in
     * parallel.
     */
    private static final Map<Double, Constant> POOL = new ConcurrentHashMap<>();

    private double value;

    public Constant(double value) {
        this.value = value;
    }

    /**
     * Returns a shared, immutable constant for {@code value}. Prefer this over the constructor for anything stored in
     * a parsed expression tree.
     */
    public static Constant of(double value) {
        Constant pooled = POOL.get(value);

        if (pooled != null) {
            return pooled;
        }

        if (POOL.size() >= MAX_POOL_SIZE) {
            return new Pooled(value);
        }

        return POOL.computeIfAbsent(value, Pooled::new);
    }

    /**
     * Drops the pool. Trees already holding pooled constants keep them; new parses start sharing again from empty.
     */
    public static void clearPool() {
        POOL.clear();
    }

    /**
     * @return the number of distinct values currently pooled
     */
    public static int poolSize() {
        return POOL.size();
    }

    @Override
    public double get() {
        return this.value;
    }

    /**
     * Changes the value of a constant made with the constructor. Pooled constants from {@link #of(double)} are shared
     * between unrelated expressions and throw instead.
     */
    public void set(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(this.value);
    }

    private static final class Pooled extends Constant {

        private Pooled(double value) {
            super(value);
        }

        @Override
        public void set(double value) {
            throw new UnsupportedOperationException("Pooled constants are shared and can't be changed");
        }
    }
}
