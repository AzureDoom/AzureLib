package mod.azure.azurelib.util;

@FunctionalInterface
public interface AzureTriFunction<T, U, V, R> {

    R apply(T t, U u, V v);
}
