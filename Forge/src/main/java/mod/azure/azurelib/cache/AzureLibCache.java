package mod.azure.azurelib.cache;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Function;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.AzureLibException;
import mod.azure.azurelib.cache.object.BakedGeoModel;
import mod.azure.azurelib.core.animatable.model.CoreGeoModel;
import mod.azure.azurelib.loading.FileLoader;
import mod.azure.azurelib.loading.json.FormatVersion;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.loading.object.BakedAnimations;
import mod.azure.azurelib.loading.object.BakedModelFactory;
import mod.azure.azurelib.loading.object.GeometryTree;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener.IStage;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModLoader;

/**
 * Cache class for holding loaded {@link mod.azure.azurelib.core.animation.Animation Animations} and {@link CoreGeoModel Models}
 */
public final class AzureLibCache {
	private static final List<String> EXCLUDED_NAMESPACES = Arrays.asList("animatedmobsmod", "moreplayermodels", "dungeons_mobs", "customnpcs", "gunsrpg", "mimic", "celestisynth", "the_flesh_that_hates", "enemyexpansion", "mutationcraft");

	private static Map<ResourceLocation, BakedAnimations> ANIMATIONS = Collections.emptyMap();
	private static Map<ResourceLocation, BakedGeoModel> MODELS = Collections.emptyMap();

	public static Map<ResourceLocation, BakedAnimations> getBakedAnimations() {
		if (!AzureLib.hasInitialized)
			throw new RuntimeException("AzureLib was never initialized! Please read the documentation!");

		return ANIMATIONS;
	}

	public static Map<ResourceLocation, BakedGeoModel> getBakedModels() {
		if (!AzureLib.hasInitialized)
			throw new RuntimeException("AzureLib was never initialized! Please read the documentation!");

		return MODELS;
	}

	public static void registerReloadListener() {
		Minecraft mc = Minecraft.getInstance();

		if (mc == null) {
			if (!ModLoader.isDataGenRunning())
				AzureLib.LOGGER.warn("Minecraft.getInstance() was null, could not register reload listeners");

			return;
		}

		if (!(mc.getResourceManager() instanceof IReloadableResourceManager))
			throw new RuntimeException("AzureLib was initialized too early!");

		((IReloadableResourceManager) mc.getResourceManager()).registerReloadListener(AzureLibCache::reload);
	}

	private static CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
		Map<ResourceLocation, BakedAnimations> animations = new Object2ObjectOpenHashMap<>();
		Map<ResourceLocation, BakedGeoModel> models = new Object2ObjectOpenHashMap<>();

		return CompletableFuture.allOf(loadAnimations(backgroundExecutor, resourceManager, animations::put), loadModels(backgroundExecutor, resourceManager, models::put)).thenCompose(stage::wait).thenAcceptAsync(empty -> {
			AzureLibCache.ANIMATIONS = animations;
			AzureLibCache.MODELS = models;
		}, gameExecutor);
	}

	private static CompletableFuture<Void> loadAnimations(Executor backgroundExecutor, IResourceManager resourceManager, BiConsumer<ResourceLocation, BakedAnimations> elementConsumer) {
		return loadResources(backgroundExecutor, resourceManager, "animations", resource -> FileLoader.loadAnimationsFile(resource, resourceManager), elementConsumer);
	}

	private static CompletableFuture<Void> loadModels(Executor backgroundExecutor, IResourceManager resourceManager, BiConsumer<ResourceLocation, BakedGeoModel> elementConsumer) {
		return loadResources(backgroundExecutor, resourceManager, "geo", resource -> {
			Model model = FileLoader.loadModelFile(resource, resourceManager);

			if (model.formatVersion() != FormatVersion.V_1_12_0)
				throw new AzureLibException(resource, "Unsupported geometry json version. Supported versions: 1.12.0");

			return BakedModelFactory.getForNamespace(resource.getNamespace()).constructGeoModel(GeometryTree.fromModel(model));
		}, elementConsumer);
	}

	private static <T> CompletableFuture<Void> loadResources(Executor executor, IResourceManager resourceManager, String type, Function<ResourceLocation, T> loader, BiConsumer<ResourceLocation, T> map) {
		return CompletableFuture.supplyAsync(() -> resourceManager.listResources(type, fileName -> fileName.toString().endsWith(".json")), executor).thenApplyAsync(resources -> {
			Map<ResourceLocation, CompletableFuture<T>> tasks = new Object2ObjectOpenHashMap<>();

			for (ResourceLocation resource : resources) {
				tasks.put(resource, CompletableFuture.supplyAsync(() -> loader.apply(resource), executor));
			}

			return tasks;
		}, executor).thenAcceptAsync(tasks -> {
			for (Entry<ResourceLocation, CompletableFuture<T>> entry : tasks.entrySet()) {
				// Skip known namespaces that use an "animation" folder as well
				if (!EXCLUDED_NAMESPACES.contains(entry.getKey().getNamespace().toLowerCase(Locale.ROOT)))
					map.accept(entry.getKey(), entry.getValue().join());
			}
		}, executor);
	}
}
