package mod.azure.azurelib.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.rewrite.AzResourceCache;
import mod.azure.azurelib.rewrite.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.rewrite.model.cache.AzBakedModelCache;

/**
 * The AzureLibCache class serves as a critical manager for registering and reloading cache components in a Minecraft
 * modding environment. It initializes and oversees the reloading process of various components, ensuring they are
 * loaded properly when resources are reloaded in the game.
 * <p>
 * This class is designed to work in tandem with other cache managers, such as {@link AzBakedAnimationCache} and
 * {@link AzBakedModelCache}, which handle specific types of resources. It ensures these resources are loaded
 * asynchronously to improve game performance and avoid blocking the main game thread.
 * <p>
 * Note: This class should only be used after ensuring the game and its resource manager have been fully initialized.
 */
public final class AzureLibCache {

    public static void registerReloadListener() {
        var mc = Minecraft.getInstance();

        if (mc == null)
            return;

        if (!(mc.getResourceManager() instanceof ReloadableResourceManager resourceManager))
            throw new AzureLibException("AzureLib was initialized too early!");

        resourceManager.registerReloadListener(AzureLibCache::reload);
    }

    public static CompletableFuture<Void> reload(
        PreparationBarrier stage,
        ResourceManager resourceManager,
        ProfilerFiller preparationsProfiler,
        ProfilerFiller reloadProfiler,
        Executor backgroundExecutor,
        Executor gameExecutor
    ) {
        return CompletableFuture
            .allOf(
                // Forward-support for new cache components
                AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager),
                AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager)
            )
            .thenCompose(stage::wait)
            .thenAcceptAsync(empty -> {}, gameExecutor);
    }

    private static <T> CompletableFuture<Void> loadResources(
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
                Map<ResourceLocation, CompletableFuture<T>> tasks = new Object2ObjectOpenHashMap<>();

                for (ResourceLocation resource : resources) {
                    tasks.put(resource, CompletableFuture.supplyAsync(() -> loader.apply(resource), executor));
                }

                return tasks;
            }, executor)
            .thenAcceptAsync(tasks -> {
                for (Entry<ResourceLocation, CompletableFuture<T>> entry : tasks.entrySet()) {
                    if (
                        !AzResourceCache.EXCLUDED_NAMESPACES.contains(
                            entry.getKey().getNamespace().toLowerCase(Locale.ROOT)
                        )
                    )
                        map.accept(entry.getKey(), entry.getValue().join());
                }
            }, executor);
    }
}
