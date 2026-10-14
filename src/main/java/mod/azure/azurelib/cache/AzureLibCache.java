package mod.azure.azurelib.cache;

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
import mod.azure.azurelib.render.texture.TextureManager;
import mod.azure.azurelib.resource.AzResourceManager;
import mod.azure.azurelib.util.ResourceLocation;

/**
 * Loads AzureLib's model and animation caches.
 * <p>
 * Beta 1.7.3 has no resource reload system. The caches are loaded once, the first time anything asks for a model or an
 * animation (see {@link #ensureLoaded()}), and can be reloaded manually with {@link #reload()} (for instance from a
 * debug key binding, or after the texture pack changes).
 */
public final class AzureLibCache {

    private static final AtomicInteger THREAD_ID = new AtomicInteger();

    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "AzureLib Resource Loader #" + THREAD_ID.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    };

    private static volatile boolean loaded;

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    /** Loads the caches if they have never been loaded. Cheap after the first call. */
    public static void ensureLoaded() {
        if (!loaded) {
            synchronized (AzureLibCache.class) {
                if (!loaded) {
                    // Set first so that a re-entrant call made while loading doesn't recurse.
                    loaded = true;
                    reload(AzResourceManager.getInstance());
                }
            }
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }

    /** Reloads every model and animation and drops cached textures. Must be called on the client thread. */
    public static void reload() {
        loaded = true;
        AzResourceManager.getInstance().invalidate();
        reload(AzResourceManager.getInstance());
        TextureManager.getInstance().clear();
    }

    private static void reload(AzResourceManager resourceManager) {
        int threads = Math.max(1, Math.min(4, Runtime.getRuntime().availableProcessors() - 1));
        ExecutorService executor = Executors.newFixedThreadPool(threads, THREAD_FACTORY);
        try {
            CompletableFuture<Map<ResourceLocation, AzBakedAnimations>> animations = AzBakedAnimationCache.getInstance()
                .loadAnimations(executor, resourceManager);
            CompletableFuture<Map<ResourceLocation, AzBakedModel>> models = AzBakedModelCache.getInstance()
                .loadModels(executor, resourceManager);

            AzBakedAnimationCache.getInstance().apply(animations.join());
            AzBakedModelCache.getInstance().apply(models.join());
            AzureLib.LOGGER.info(
                "Loaded {} AzureLib models and {} animation files",
                AzBakedModelCache.getInstance().size(),
                AzBakedAnimationCache.getInstance().size()
            );
        } catch (Exception e) {
            AzureLib.LOGGER.error("Failed to load AzureLib models and animations", e);
        } finally {
            executor.shutdown();
        }

        AnimatableTexture.onResourceReload();
        AzItemDisplayTransforms.clearCache();
    }
}
