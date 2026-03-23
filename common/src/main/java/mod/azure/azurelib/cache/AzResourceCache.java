package mod.azure.azurelib.cache;

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
        "block_factorys_bosses"
    );

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
