package mod.azure.azurelib.platform.services;

import net.minecraft.core.Registry;

import java.util.function.Supplier;

public interface CommonRegistry {

    <T> Supplier<T> register(
        Registry<? super T> registry,
        String registryName,
        Supplier<? extends T> supplier
    );
}
