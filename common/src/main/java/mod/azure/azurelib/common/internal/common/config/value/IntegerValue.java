package mod.azure.azurelib.common.internal.common.config.value;

import mod.azure.azurelib.common.internal.common.config.Configurable;

import java.lang.reflect.Field;
import java.util.Objects;

public abstract class IntegerValue<N extends Number> extends ConfigValue<N> {

    protected Range range;

    public IntegerValue(ValueData<N> valueData, Range range) {
        super(valueData);
        this.range = Objects.requireNonNull(range);
    }

    @Override
    protected void readFieldData(Field field) {
        super.readFieldData(field);
        Configurable.Range intRange = field.getAnnotation(Configurable.Range.class);
        if (intRange != null) {
            this.range = Range.newBoundedRange(intRange.min(), intRange.max());
        }
    }

    @Override
    public abstract N getCorrectedValue(N in);

    public Range getRange() {
        return range;
    }

    public static final class Range {

        private final long min, max;

        private Range(long min, long max) {
            this.min = min;
            this.max = max;
        }

        public static Range newBoundedRange(long min, long max) {
            if (min > max) {
                throw new IllegalArgumentException(String.format("Invalid number range: Min value (%d) cannot be bigger than max value (%d)", min, max));
            }
            return new Range(min, max);
        }

        public static Range unboundedLong() {
            return newBoundedRange(Long.MIN_VALUE, Long.MAX_VALUE);
        }

        public static Range unboundedInt() {
            return newBoundedRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        }

        public boolean isWithin(long number) {
            return number >= min && number <= max;
        }

        public long min() {
            return this.min;
        }

        public long max() {
            return this.max;
        }

        public long clamp(long in) {
            return Math.min(max, Math.max(min, in));
        }

        public int clamp(int in) {
            return (int) Math.min(max, Math.max(min, in));
        }
    }
}
