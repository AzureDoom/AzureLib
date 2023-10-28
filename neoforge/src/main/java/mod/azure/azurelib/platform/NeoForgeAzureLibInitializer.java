package mod.azure.azurelib.platform;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.platform.services.AzureLibInitializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {
    @Override
    public void initialize() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AzureLibCache::registerReloadListener);
        Services.NETWORK.registerClientReceiverPackets();
    }
}