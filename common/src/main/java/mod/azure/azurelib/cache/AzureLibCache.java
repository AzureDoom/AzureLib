/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.cache;

import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.model.cache.AzBakedModelCache;
import mod.azure.azurelib.util.AzureLibException;

public final class AzureLibCache {

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    public static void registerReloadListener() {
        Minecraft mc = Minecraft.getInstance();

        if (mc == null) {
            return;
        }

        if (!(mc.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            throw new AzureLibException("AzureLib was initialized too early!");
        }

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
