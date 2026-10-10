package mod.azure.azurelib.util.function;

/**
 * A {@code double -> double} function. Replaces fastutil's {@code Double2DoubleFunction} used on later versions;
 * Minecraft 1.7.10 does not ship fastutil.
 */
@FunctionalInterface
public interface Double2DoubleFunction {

    double get(double value);
}
