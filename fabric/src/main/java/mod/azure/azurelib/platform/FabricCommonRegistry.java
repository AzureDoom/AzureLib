package mod.azure.azurelib.platform;

import net.minecraft.core.Registry;

import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.services.CommonRegistry;

public class FabricCommonRegistry implements CommonRegistry {

    private static <T, R extends Registry<? super T>> Supplier<T> registerSupplier(
        R registry,
        String id,
        Supplier<T> object
    ) {
        final T registeredObject = Registry.register(
            (Registry<T>) registry,
            AzureLib.modResource(id),
            object.get()
        );

        return () -> registeredObject;
    }

    @Override
    public <T> Supplier<T> register(Registry<? super T> registry, String registryName, Supplier<? extends T> supplier) {
        return (Supplier<T>) registerSupplier(registry, registryName, supplier);
    }
}
