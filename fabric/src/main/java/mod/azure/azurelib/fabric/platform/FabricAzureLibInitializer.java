package mod.azure.azurelib.fabric.platform;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class FabricAzureLibInitializer implements AzureLibInitializer {

    // TODO: Fix for 26.2
    @Override
    public void initialize() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
            .registerReloadListener(new IdentifiableResourceReloadListener() {

                @Override
                public @NonNull CompletableFuture<Void> reload(
                    @NonNull SharedState currentReload,
                    @NonNull Executor taskExecutor,
                    PreparationBarrier preparationBarrier,
                    Executor reloadExecutor
                ) {
                    return AzureLibCache.reload(
                        synchronizer,
                        manager,
                        prepareProfiler,
                        applyProfiler,
                        prepareExecutor,
                        applyExecutor
                    );
                }

                @Override
                public @NonNull Identifier getFabricId() {
                    return AzureLib.modResource("models");
                }
            });
    }
}
