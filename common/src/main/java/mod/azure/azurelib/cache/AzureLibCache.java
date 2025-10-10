package mod.azure.azurelib.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.model.cache.AzBakedModelCache;

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
                AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager),
                AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager)
            )
            .thenCompose(stage::wait)
            .thenAcceptAsync(empty -> {}, gameExecutor);
    }
}
