package mod.azure.azurelib.common.animation.cache;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.common.animation.primitive.AzBakedAnimations;
import mod.azure.azurelib.common.cache.AzResourceCache;
import mod.azure.azurelib.common.loading.FileLoader;
import mod.azure.azurelib.core.molang.MolangParser;

/**
 * AzBakedAnimationCache is a singleton cache to manage and store preloaded animation data of type
 * {@link AzBakedAnimations}. It is an extension of {@link AzResourceCache} and provides mechanisms for managing
 * animation resources in Minecraft modding. Aimed at efficient storage and retrieval, as well as background processing
 * of animation data. <br>
 * Features:
 * <ul>
 * <li>Supports asynchronous loading of animation resources from the in-memory {@code ResourceManager}.
 * <li>Caches animation data keyed by {@link ResourceLocation}.
 * <li>Replaces the whole cache atomically on reload, so readers never see a partially loaded map.</li>
 * <li>Provides access to the cached animations or null values for non-existent records.</li>
 * </ul>
 */
public class AzBakedAnimationCache extends AzResourceCache {

    private static final AzBakedAnimationCache INSTANCE = new AzBakedAnimationCache();

    public static AzBakedAnimationCache getInstance() {
        return INSTANCE;
    }

    private volatile Map<ResourceLocation, AzBakedAnimations> bakedAnimations = Map.of();

    private AzBakedAnimationCache() {}

    public CompletableFuture<Map<ResourceLocation, AzBakedAnimations>> loadAnimations(
        Executor backgroundExecutor,
        ResourceManager resourceManager
    ) {
        MolangParser.clearExpressionCache();

        return loadResources(
            backgroundExecutor,
            resourceManager,
            "animations",
            resource -> FileLoader.loadAzAnimationsFile(resource, resourceManager)
        );
    }

    public void apply(Map<ResourceLocation, AzBakedAnimations> animations) {
        this.bakedAnimations = animations;
    }

    public @Nullable AzBakedAnimations getNullable(ResourceLocation resourceLocation) {
        return bakedAnimations.get(resourceLocation);
    }
}
