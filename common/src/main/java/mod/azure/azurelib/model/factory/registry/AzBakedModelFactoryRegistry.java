package mod.azure.azurelib.model.factory.registry;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

import mod.azure.azurelib.model.factory.AzBakedModelFactory;
import mod.azure.azurelib.model.factory.impl.AzBuiltinBakedModelFactory;

/**
 * A registry for managing instances of {@link AzBakedModelFactory} that are used to handle the creation of baked models
 * for specific namespaces. This allows custom behavior for different mods or namespaces when constructing models. <br>
 * This class provides functionality to register, retrieve, and manage baked model factories. It ensures that a default
 * factory is available for any namespace that does not explicitly register a custom factory.
 */
public class AzBakedModelFactoryRegistry {

    private static final Map<String, AzBakedModelFactory> FACTORIES = new Object2ObjectOpenHashMap<>(1);

    private static final AzBakedModelFactory DEFAULT_FACTORY = new AzBuiltinBakedModelFactory();

    public static AzBakedModelFactory getForNamespace(String namespace) {
        return FACTORIES.getOrDefault(namespace, DEFAULT_FACTORY);
    }

    /**
     * Register a custom {@link AzBakedModelFactory} to handle loading models in a custom way.<br>
     * <b><u>MUST be called during mod construct</u></b><br>
     *
     * @param namespace The namespace (modid) to register the factory for
     * @param factory   The factory responsible for model loading under the given namespace
     */
    public static void register(String namespace, AzBakedModelFactory factory) {
        FACTORIES.put(namespace, factory);
    }
}
