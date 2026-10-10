package mod.azure.azurelib.cache;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;

/**
 * Base class for AzureLib's resource caches (models and animations).
 */
public abstract class AzResourceCache {

    public static final Set<String> EXCLUDED_NAMESPACES = Collections.unmodifiableSet(
        new HashSet<>(
            Arrays.asList(
                "moreplayermodels",
                "customnpcs",
                "creeperoverhaul",
                "geckolib",
                "gunsrpg",
                "born_in_chaos_v1",
                "neoforge",
                "brutality",
                "crazythings",
                "block_factorys_bosses"
            )
        )
    );

    /**
     * Loads every {@code .json} resource under {@code assets/<namespace>/<type>/} into a map.
     * <p>
     * 1.7.10's {@link IResourceManager} cannot list resources, so the candidate locations are found by
     * {@link AzResourceLister}; each one is then read through the resource manager so normal resource pack priority
     * applies.
     *
     * @param executor        the executor used to load and parse individual files
     * @param resourceManager the resource manager to read from
     * @param type            the asset folder to scan, e.g. {@code "geo"} or {@code "animations"}
     * @param loader          parses a single resource; returning {@code null} skips it
     */
    protected final <T> CompletableFuture<Map<ResourceLocation, T>> loadResources(
        Executor executor,
        IResourceManager resourceManager,
        String type,
        Function<ResourceLocation, T> loader
    ) {
        Set<ResourceLocation> resources = AzResourceLister.listResources(resourceManager, type, ".json");
        Map<ResourceLocation, CompletableFuture<T>> tasks = new HashMap<>();

        for (ResourceLocation resource : resources) {
            if (EXCLUDED_NAMESPACES.contains(resource.getResourceDomain().toLowerCase(Locale.ROOT)))
                continue;

            tasks.put(
                resource,
                CompletableFuture.supplyAsync(() -> loader.apply(resource), executor).exceptionally(throwable -> {
                    AzureLib.LOGGER.error("Failed to load {}: skipping", resource, throwable);
                    return null;
                })
            );
        }

        return CompletableFuture.allOf(tasks.values().toArray(new CompletableFuture[0])).thenApply(ignored -> {
            Map<ResourceLocation, T> loaded = new HashMap<>(tasks.size());
            for (Map.Entry<ResourceLocation, CompletableFuture<T>> entry : tasks.entrySet()) {
                T value = entry.getValue().join();
                if (value != null)
                    loaded.put(entry.getKey(), value);
            }
            return loaded;
        });
    }
}
