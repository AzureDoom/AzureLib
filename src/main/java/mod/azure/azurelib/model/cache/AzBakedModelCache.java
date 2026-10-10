package mod.azure.azurelib.model.cache;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.AzResourceCache;
import mod.azure.azurelib.loading.FileLoader;
import mod.azure.azurelib.loading.json.raw.Model;
import mod.azure.azurelib.loading.object.GeometryTree;
import mod.azure.azurelib.model.AzBakedModel;
import mod.azure.azurelib.model.factory.registry.AzBakedModelFactoryRegistry;

/**
 * AzBakedModelCache is a singleton class that extends {@link AzResourceCache} and is designed to manage and cache baked
 * models of type {@link AzBakedModel}. It provides functionality to asynchronously load and store models associated
 * with specific resource locations.
 */
public class AzBakedModelCache extends AzResourceCache {

    private static final AzBakedModelCache INSTANCE = new AzBakedModelCache();

    public static AzBakedModelCache getInstance() {
        return INSTANCE;
    }

    private volatile Map<ResourceLocation, AzBakedModel> bakedModels = java.util.Collections.emptyMap();

    private AzBakedModelCache() {}

    public CompletableFuture<Map<ResourceLocation, AzBakedModel>> loadModels(
        Executor backgroundExecutor,
        IResourceManager resourceManager
    ) {
        return loadResources(backgroundExecutor, resourceManager, "geo", resource -> {
            Model model = FileLoader.loadModelFile(resource, resourceManager);

            if (model == null) {
                ResourceLocation defaultModelLocation = AzureLib.modResource("geo/default_model.geo.json");
                model = FileLoader.loadModelFile(defaultModelLocation, resourceManager);
                AzBakedModel defaultBaked = AzBakedModelFactoryRegistry
                    .getForNamespace(resource.getResourceDomain())
                    .constructGeoModel(GeometryTree.fromModel(model));

                AzBakedModel.setDefault(defaultBaked);
            }

            return AzBakedModelFactoryRegistry.getForNamespace(resource.getResourceDomain())
                .constructGeoModel(GeometryTree.fromModel(model));
        });
    }

    public void apply(Map<ResourceLocation, AzBakedModel> models) {
        this.bakedModels = models;
    }

    public @Nullable AzBakedModel getNullable(ResourceLocation resourceLocation) {
        return bakedModels.get(resourceLocation);
    }
}
