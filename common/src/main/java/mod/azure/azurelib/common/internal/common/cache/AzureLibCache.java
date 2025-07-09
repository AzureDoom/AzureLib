/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.loading.FileLoader;
import mod.azure.azurelib.common.internal.common.loading.json.raw.Model;
import mod.azure.azurelib.common.internal.common.loading.object.BakedAnimations;
import mod.azure.azurelib.common.internal.common.loading.object.BakedModelFactory;
import mod.azure.azurelib.common.internal.common.loading.object.GeometryTree;
import mod.azure.azurelib.core.animatable.model.CoreGeoModel;
import mod.azure.azurelib.core.animation.Animation;
import mod.azure.azurelib.rewrite.AzResourceCache;
import mod.azure.azurelib.rewrite.animation.cache.AzBakedAnimationCache;
import mod.azure.azurelib.rewrite.model.cache.AzBakedModelCache;

/**
 * Cache class for holding loaded {@link Animation Animations} and {@link CoreGeoModel Models}
 *
 * @deprecated Use {@link AzBakedModelCache} and {@link AzBakedAnimationCache} instead.
 */
public final class AzureLibCache {

    /**
     * @deprecated
     */
    private static Map<ResourceLocation, BakedAnimations> ANIMATIONS = Collections.emptyMap();

    /**
     * @deprecated
     */
    private static Map<ResourceLocation, BakedGeoModel> MODELS = Collections.emptyMap();

    private AzureLibCache() {
        throw new UnsupportedOperationException();
    }

    /**
     * @deprecated Use {@link AzBakedAnimationCache} instead.
     */
    public static Map<ResourceLocation, BakedAnimations> getBakedAnimations() {
        if (!AzureLib.hasInitialized)
            throw new AzureLibException("AzureLib was never initialized! Please read the documentation!");

        return ANIMATIONS;
    }

    /**
     * @deprecated Use {@link AzBakedModelCache} instead.
     */
    public static Map<ResourceLocation, BakedGeoModel> getBakedModels() {
        if (!AzureLib.hasInitialized)
            throw new AzureLibException("AzureLib was never initialized! Please read the documentation!");

        return MODELS;
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
        // TODO: Remove these.
        Map<ResourceLocation, BakedAnimations> animations = new Object2ObjectOpenHashMap<>();
        Map<ResourceLocation, BakedGeoModel> models = new Object2ObjectOpenHashMap<>();

        return CompletableFuture
            .allOf(
                // TODO: Remove these.
                loadAnimations(backgroundExecutor, resourceManager, animations::put),
                loadModels(backgroundExecutor, resourceManager, models::put),
                // Forward-support for new cache components
                AzBakedAnimationCache.getInstance().loadAnimations(backgroundExecutor, resourceManager),
                AzBakedModelCache.getInstance().loadModels(backgroundExecutor, resourceManager)
            )
            .thenCompose(stage::wait)
            .thenAcceptAsync(empty -> {
                AzureLibCache.ANIMATIONS = animations;
                AzureLibCache.MODELS = models;
            }, gameExecutor);
    }

    /**
     * @deprecated
     */
    private static CompletableFuture<Void> loadAnimations(
        Executor backgroundExecutor,
        ResourceManager resourceManager,
        BiConsumer<ResourceLocation, BakedAnimations> elementConsumer
    ) {
        return loadResources(
            backgroundExecutor,
            resourceManager,
            "animations",
            resource -> FileLoader.loadAnimationsFile(resource, resourceManager),
            elementConsumer
        );
    }

    /**
     * @deprecated
     */
    private static CompletableFuture<Void> loadModels(
        Executor backgroundExecutor,
        ResourceManager resourceManager,
        BiConsumer<ResourceLocation, BakedGeoModel> elementConsumer
    ) {
        return loadResources(backgroundExecutor, resourceManager, "geo", resource -> {
            Model model = FileLoader.loadModelFile(resource, resourceManager);

            return BakedModelFactory.getForNamespace(resource.getNamespace())
                .constructGeoModel(GeometryTree.fromModel(model));
        }, elementConsumer);
    }

    /**
     * @deprecated
     */
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

                for (ResourceLocation resource : resources.keySet()) {
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
