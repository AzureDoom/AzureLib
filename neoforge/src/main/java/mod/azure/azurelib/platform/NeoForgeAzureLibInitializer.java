package mod.azure.azurelib.platform;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.platform.services.AzureLibInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {
    @Override
    public void initialize() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AzureLibCache.registerReloadListener();
        }
        Services.NETWORK.registerClientReceiverPackets();
    }
}