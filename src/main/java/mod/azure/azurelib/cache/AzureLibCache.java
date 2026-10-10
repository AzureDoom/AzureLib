package mod.azure.azurelib.cache;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.cache.texture.AnimatableTexture;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.cache.AzBakedModelCache;
import mod.azure.azurelib.render.item.AzItemDisplayTransforms;
import mod.azure.azurelib.util.AzureLibException;

/**
 * Hooks AzureLib's model and animation caches into 1.7.10's resource reloading.
 * <p>
 * 1.7.10 reload listeners are synchronous and run on the client thread, so files are parsed on a small worker pool and
 * the listener blocks until they're done before swapping the caches in.
 */
@SideOnly(Side.CLIENT)
public final class AzureLibCache {

    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "AzureLib Resource Loader #" + THREAD_ID.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    };

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    public static void registerReloadListener() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null)
            return;

        if (!(mc.getResourceManager() instanceof IReloadableResourceManager))
            throw new AzureLibException("AzureLib was initialized too early!");

        ((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(AzureLibCache::reload);
    }

    public static void reload(IResourceManager resourceManager) {
        int threads = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1));
        ExecutorService executor = Executors.newFixedThreadPool(threads, THREAD_FACTORY);
        try {
            CompletableFuture<Map<ResourceLocation, AzBakedAnimations>> animations = AzBakedAnimationCache.getInstance()
                .loadAnimations(executor, resourceManager);
            CompletableFuture<Map<ResourceLocation, AzBakedModel>> models = AzBakedModelCache.getInstance()
                .loadModels(executor, resourceManager);

            AzBakedAnimationCache.getInstance().apply(animations.join());
            AzBakedModelCache.getInstance().apply(models.join());
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to reload AzureLib models and animations", e);
        } finally {
            executor.shutdown();
        }

        AnimatableTexture.onResourceReload();
        AzItemDisplayTransforms.clearCache();
    }
}
