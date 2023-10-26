package mod.azure.azurelib;

import mod.azure.azurelib.cache.AzureLibCache;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class AzureLib {
	public static final Logger LOGGER = LogManager.getLogger("azurelib");
	public static final Marker MAIN_MARKER = MarkerManager.getMarker("main");
	public static final String MOD_ID = "azurelib";
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
						public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier synchronizer, ResourceManager manager,
															  ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler, Executor prepareExecutor,
															  Executor applyExecutor) {
							return AzureLibCache.reload(synchronizer, manager, prepareProfiler,
									applyProfiler, prepareExecutor, applyExecutor);
						}
					});
		}
		hasInitialized = true;
	}
}
