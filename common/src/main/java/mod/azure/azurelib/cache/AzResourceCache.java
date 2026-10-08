package mod.azure.azurelib.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;

/**
 * AzResourceCache is an abstract base class designed for managing and loading mod resources asynchronously. This class
 * provides helper functions for loading and processing resource files of a specific type and storing them in a cache.
 */
public abstract class AzResourceCache {

    /**
     * A set of namespaces that should be excluded when processing or loading resources. These namespaces are
     * pre-defined and typically represent mods or resource groups that are not intended to be processed by the resource
     * management logic of the AzResourceCache class.
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
        "crazythings",
        "twilightforest"
    );

    /**
     * Asynchronously loads resources from the provided {@code ResourceManager} based on the specified {@code type} into
     * a new map. Nothing shared is modified, so the returned map can be swapped in atomically on the game thread.
     * <p>
     * Resources from excluded namespaces are skipped before any work is scheduled. A resource whose loader throws or
     * returns {@code null} is logged and left out of the map, without affecting any other resource.
     *
     * @param <T>             The type of the resource being loaded and processed.
     * @param executor        The executor used to execute asynchronous tasks.
     * @param resourceManager The resource manager used to locate and manage resources.
     * @param type            The type of resource to be fetched, typically a folder or category defined in the resource
     *                        pack (e.g., "animations").
     * @param loader          A function that processes a {@link Identifier} into an object of type {@code T}.
     * @return A {@code CompletableFuture} that completes with every successfully loaded resource, keyed by
     *         {@link Identifier}.
     */
    protected final <T> CompletableFuture<Map<Identifier, T>> loadResources(
        Executor executor,
        ResourceManager resourceManager,
        String type,
        Function<Identifier, T> loader
    ) {
        return CompletableFuture.supplyAsync(
            () -> resourceManager.listResources(type, fileName -> fileName.toString().endsWith(".json")),
            executor
        )
            .thenCompose(resources -> {
                var tasks = new Object2ObjectOpenHashMap<Identifier, CompletableFuture<T>>();

                for (var resource : resources.keySet()) {
                    if (EXCLUDED_NAMESPACES.contains(resource.getNamespace().toLowerCase(Locale.ROOT)))
                        continue;

                    tasks.put(
                        resource,
                        CompletableFuture.supplyAsync(() -> loader.apply(resource), executor)
                            .exceptionally(throwable -> {
                                AzureLib.LOGGER.error("Failed to load {}: skipping", resource, throwable);
                                return null;
                            })
                    );
                }

                return CompletableFuture.allOf(tasks.values().toArray(CompletableFuture[]::new))
                    .thenApply(ignored -> {
                        var loaded = new Object2ObjectOpenHashMap<Identifier, T>(tasks.size());

                        for (var entry : tasks.entrySet()) {
                            T value = entry.getValue().join();

                            if (value != null)
                                loaded.put(entry.getKey(), value);
                        }

                        return loaded;
                    });
            });
    }
}
