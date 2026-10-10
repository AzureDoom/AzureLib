package mod.azure.azurelib.util.function;

/**
 * A {@code double -> double} function. Replaces fastutil's {@code Double2DoubleFunction}, which is not a functional
 * interface in the fastutil 7 build bundled with Minecraft 1.12.2.
 */
@FunctionalInterface
public interface Double2DoubleFunction {

    double get(double value);
}
