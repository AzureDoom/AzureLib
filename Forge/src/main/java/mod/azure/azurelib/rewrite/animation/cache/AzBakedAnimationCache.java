package mod.azure.azurelib.rewrite.animation.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.loading.FileLoader;
import mod.azure.azurelib.rewrite.AzResourceCache;
import mod.azure.azurelib.rewrite.animation.primitive.AzBakedAnimations;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * AzBakedAnimationCache is a singleton cache to manage and store preloaded animation data of type
 * {@link AzBakedAnimations}. It is an extension of {@link AzResourceCache} and provides mechanisms for managing
 * animation resources in Minecraft modding. Aimed at efficient storage and retrieval, as well as background processing
 * of animation data. <br>
 * Features:
 * <ul>
 * <li>Supports asynchronous loading of animation resources from the in-memory {@code ResourceManager}.
 * <li>Caches animation data keyed by {@link ResourceLocation}.
 * <li>Provides access to the cached animations or null values for non-existent records.</li>
 * </ul>
 */
public class AzBakedAnimationCache extends AzResourceCache {

    private static final AzBakedAnimationCache INSTANCE = new AzBakedAnimationCache();

    public static AzBakedAnimationCache getInstance() {
        return INSTANCE;
    }

    private final Map<ResourceLocation, AzBakedAnimations> bakedAnimations;

    private AzBakedAnimationCache() {
        this.bakedAnimations = new Object2ObjectOpenHashMap<>();
    }

    public CompletableFuture<Void> loadAnimations(Executor backgroundExecutor, IResourceManager resourceManager) {
        return loadResources(
            backgroundExecutor,
            resourceManager,
            "animations",
            resource -> FileLoader.loadAzAnimationsFile(resource, resourceManager),
            bakedAnimations::put
        );
    }

    public AzBakedAnimations getNullable(ResourceLocation resourceLocation) {
        return bakedAnimations.get(resourceLocation);
    }
}
