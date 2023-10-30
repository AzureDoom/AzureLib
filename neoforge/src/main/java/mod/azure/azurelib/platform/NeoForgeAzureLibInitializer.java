package mod.azure.azurelib.platform;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.platform.services.AzureLibInitializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.DistExecutor;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {
    @Override
    public void initialize() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AzureLibCache::registerReloadListener);
        Services.NETWORK.registerClientReceiverPackets();
    }
}