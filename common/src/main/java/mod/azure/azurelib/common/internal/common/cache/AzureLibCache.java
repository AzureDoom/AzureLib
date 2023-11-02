package mod.azure.azurelib.common.internal.common.cache;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import mod.azure.azurelib.common.internal.common.core.animatable.model.CoreGeoModel;
import mod.azure.azurelib.common.internal.common.core.animation.Animation;
import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.AzureLibException;
import mod.azure.azurelib.common.internal.common.cache.object.BakedGeoModel;
import mod.azure.azurelib.common.internal.common.loading.FileLoader;
import mod.azure.azurelib.common.internal.common.loading.json.FormatVersion;
import mod.azure.azurelib.common.internal.common.loading.json.raw.Model;
import mod.azure.azurelib.common.internal.common.loading.object.BakedAnimations;
import mod.azure.azurelib.common.internal.common.loading.object.BakedModelFactory;
import mod.azure.azurelib.common.internal.common.loading.object.GeometryTree;
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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Cache class for holding loaded
 * {@link Animation Animations} and
 * {@link CoreGeoModel Models}
 */
public final class AzureLibCache {
    private static final Set<String> EXCLUDED_NAMESPACES = ObjectOpenHashSet.of("moreplayermodels", "customnpcs", "gunsrpg");

    private static Map<ResourceLocation, BakedAnimations> ANIMATIONS = Collections.emptyMap();
    private static Map<ResourceLocation, BakedGeoModel> MODELS = Collections.emptyMap();

    public static Map<ResourceLocation, BakedAnimations> getBakedAnimations() {
        if (!AzureLib.hasInitialized)
            throw new AzureLibException("AzureLib was never initialized! Please read the documentation!");

        return ANIMATIONS;
    }

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

        if (!(mc.getResourceManager() instanceof ReloadableResourceManager resourceManager))
            throw new AzureLibException("AzureLib was initialized too early!");

        resourceManager.registerReloadListener(AzureLibCache::reload);
    }

    public static CompletableFuture<Void> reload(PreparationBarrier stage, ResourceManager resourceManager,
                                                 ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor,
                                                 Executor gameExecutor) {
        Map<ResourceLocation, BakedAnimations> animations = new Object2ObjectOpenHashMap<>();
        Map<ResourceLocation, BakedGeoModel> models = new Object2ObjectOpenHashMap<>();

        return CompletableFuture
                .allOf(loadAnimations(backgroundExecutor, resourceManager, animations::put),
                        loadModels(backgroundExecutor, resourceManager, models::put))
                .thenCompose(stage::wait).thenAcceptAsync(empty -> {
                    AzureLibCache.ANIMATIONS = animations;
                    AzureLibCache.MODELS = models;
                }, gameExecutor);
    }

    private static CompletableFuture<Void> loadAnimations(Executor backgroundExecutor, ResourceManager resourceManager,
                                                          BiConsumer<ResourceLocation, BakedAnimations> elementConsumer) {
        return loadResources(backgroundExecutor, resourceManager, "animations",
                resource -> FileLoader.loadAnimationsFile(resource, resourceManager), elementConsumer);
    }

    private static CompletableFuture<Void> loadModels(Executor backgroundExecutor, ResourceManager resourceManager,
                                                      BiConsumer<ResourceLocation, BakedGeoModel> elementConsumer) {
        return loadResources(backgroundExecutor, resourceManager, "geo", resource -> {
            Model model = FileLoader.loadModelFile(resource, resourceManager);

            if (model.formatVersion() != FormatVersion.V_1_12_0)
                throw new AzureLibException(resource, "Unsupported geometry json version. Supported versions: 1.12.0");

            return BakedModelFactory.getForNamespace(resource.getNamespace())
                    .constructGeoModel(GeometryTree.fromModel(model));
        }, elementConsumer);
    }

    private static <T> CompletableFuture<Void> loadResources(Executor executor, ResourceManager resourceManager,
                                                             String type, Function<ResourceLocation, T> loader, BiConsumer<ResourceLocation, T> map) {
        return CompletableFuture.supplyAsync(
                        () -> resourceManager.listResources(type, fileName -> fileName.toString().endsWith(".json")), executor)
                .thenApplyAsync(resources -> {
                    Map<ResourceLocation, CompletableFuture<T>> tasks = new Object2ObjectOpenHashMap<>();

                    for (ResourceLocation resource : resources.keySet()) {
                        tasks.put(resource, CompletableFuture.supplyAsync(() -> loader.apply(resource), executor));
                    }

                    return tasks;
                }, executor).thenAcceptAsync(tasks -> {
                    for (Entry<ResourceLocation, CompletableFuture<T>> entry : tasks.entrySet()) {
                        if (!EXCLUDED_NAMESPACES.contains(entry.getKey().getNamespace().toLowerCase(Locale.ROOT)))
                            map.accept(entry.getKey(), entry.getValue().join());
                    }
                }, executor);
    }
}
