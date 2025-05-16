/*
 * Copyright (c) 2020. Author: Bernie G. (Gecko)
 */

package mod.azure.azurelib;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import mod.azure.azurelib.cache.AzureLibCache;

public class AzureLib {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final org.apache.logging.log4j.Marker MAIN_MARKER = org.apache.logging.log4j.MarkerManager.getMarker(
        "main"
    );

    public static final String MOD_ID = "azurelib";

    public static final String ITEM_UUID_TAG = "az_id";

    public static boolean hasInitialized;

    public static void initialize() {
        if (!hasInitialized) {
            ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)
                .registerReloadListener(new IdentifiableResourceReloadListener() {

                    @Override
                    public ResourceLocation getFabricId() {
                        return new ResourceLocation(AzureLib.MOD_ID, "models");
                    }

                    @Override
                    public CompletableFuture<Void> reload(
                        PreparationBarrier synchronizer,
                        ResourceManager manager,
                        ProfilerFiller prepareProfiler,
                        ProfilerFiller applyProfiler,
                        Executor prepareExecutor,
                        Executor applyExecutor
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
                });
        }
        hasInitialized = true;
    }

    public static ResourceLocation modResource(String name) {
        return new ResourceLocation(MOD_ID, name);
    }
}
