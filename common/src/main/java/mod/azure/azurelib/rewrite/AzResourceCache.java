package mod.azure.azurelib.rewrite;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * AzResourceCache is an abstract base class designed for managing and loading mod resources asynchronously. This class
 * provides helper functions for loading and processing resource files of a specific type and storing them in a cache.
 */
public abstract class AzResourceCache {

    /**
     * A set of namespaces that should be excluded when processing or loading resources.
     * These namespaces are pre-defined and typically represent mods or resource groups
     * that are not intended to be processed by the resource management logic of the
     * AzResourceCache class.
     */
    public static final Set<String> EXCLUDED_NAMESPACES = ObjectOpenHashSet.of(
        "moreplayermodels",
        "customnpcs",
        "creeperoverhaul",
        "geckolib",
        "gunsrpg",
        "born_in_chaos_v1",
        "neoforge",
        "brutality",
        "crazythings"
    );

    /**
     * Asynchronously loads resources from the provided {@code ResourceManager} based on the specified {@code type}.
     * The method filters resource files, processes them using the given {@code loader}, and maps the resulting objects
     * using the provided {@code map} function. Resources from excluded namespaces are ignored during processing.
     *
     * @param <T>            The type of the resource being loaded and processed.
     * @param executor       The executor used to execute asynchronous tasks.
     * @param resourceManager The resource manager used to locate and manage resources.
     * @param type           The type of resource to be fetched, typically a folder or category defined in the
     *                       resource pack (e.g., "animations").
     * @param loader         A function that processes a {@link ResourceLocation} into an object of type {@code T}.
     * @param map            A consumer that maps the processed resources (keyed by {@link ResourceLocation}) to
     *                       their corresponding values of type {@code T}.
     * @return A {@code CompletableFuture<Void>} that completes when all resources of the specified type are
     *         loaded and processed.
     */
    protected final <T> CompletableFuture<Void> loadResources(
        Executor executor,
        ResourceManager resourceManager,
        String type,
        Function<ResourceLocation, T> loader,
        BiConsumer<ResourceLocation, T> map
    ) {
        return CompletableFuture.supplyAsync(
            () -> resourceManager.listResources(type, fileName -> fileName.toString().endsWith(".json")),
            executor
        )
            .thenApplyAsync(resources -> {
                var tasks = new Object2ObjectOpenHashMap<ResourceLocation, CompletableFuture<T>>();

                for (var resource : resources.keySet()) {
                    tasks.put(resource, CompletableFuture.supplyAsync(() -> loader.apply(resource), executor));
                }

                return tasks;
            }, executor)
            .thenAcceptAsync(tasks -> {
                for (var entry : tasks.entrySet()) {
                    if (!EXCLUDED_NAMESPACES.contains(entry.getKey().getNamespace().toLowerCase(Locale.ROOT))) {
                        map.accept(entry.getKey(), entry.getValue().join());
                    }
                }
            }, executor);
    }
}
