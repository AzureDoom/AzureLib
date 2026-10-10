package mod.azure.azurelib.util.function;

/**
 * Three-argument function. Replaces commons-lang3's {@code TriFunction}, which is newer than the commons-lang3 3.3.2
 * bundled with Minecraft 1.7.10.
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);
}
