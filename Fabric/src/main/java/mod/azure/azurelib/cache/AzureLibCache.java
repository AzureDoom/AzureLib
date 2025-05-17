package mod.azure.azurelib.cache;

import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.model.cache.AzBakedModelCache;

/**
 * The AzureLibCache class serves as a utility for managing the reloading of cache resources in the AzureLib framework.
 * It provides a static method for asynchronously loading and reloading baked animations and model resources.
 * <p>
 * This functionality is crucial in ensuring that cached data is properly synchronized whenever a reload is triggered,
 * making assets like animations and models accessible in a resource-efficient manner.
 * <p>
 * The reload process leverages the {@link AzBakedAnimationCache} and {@link AzBakedModelCache} classes to load
 * animation and model resources in the background, allowing efficient usage of executors for asynchronous processing.
 * Once the resources are loaded, the method waits for a preparation barrier to synchronize and completes execution on
 * the game thread.
 */
public final class AzureLibCache {

    public static CompletableFuture<Void> reload(
        PreparationBarrier stage,
        ResourceManager resourceManager,
        ProfilerFiller preparationsProfiler,
        ProfilerFiller reloadProfiler,
        Executor backgroundExecutor,
        Executor gameExecutor
    ) {
        return CompletableFuture.allOf(
            AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager),
            AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager)
        ).thenCompose(stage::wait).thenAcceptAsync(empty -> {}, gameExecutor);
    }
}
