package mod.azure.azurelib.fabric.platform;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.cache.AzureLibCache;
import mod.azure.azurelib.common.platform.services.AzureLibInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FabricAzureLibInitializer implements AzureLibInitializer {
    @Override
    public void initialize() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return AzureLib.modResource("models");
                    }

                    @Override
                    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager manager,
                                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor,
                                                          Executor applyExecutor) {
                        return AzureLibCache.reload(synchronizer, manager, prepareProfiler,
                                applyProfiler, prepareExecutor, applyExecutor);
                    }
                });
    }
}