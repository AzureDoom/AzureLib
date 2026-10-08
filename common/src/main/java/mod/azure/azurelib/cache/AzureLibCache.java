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
        var animations = AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager);
        var models = AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager);

        return CompletableFuture.allOf(animations, models)
            .thenCompose(stage::wait)
            .thenAcceptAsync(ignored -> {
                AzBakedAnimationCache.getInstance().apply(animations.join());
                AzBakedModelCache.getInstance().apply(models.join());
            }, gameExecutor);
    }
}
