package mod.azure.azurelib.neoforge.platform;

import mod.azure.azurelib.common.internal.common.cache.AzureLibCache;
import mod.azure.azurelib.common.platform.services.AzureLibInitializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {
    @Override
    public void initialize() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            AzureLibCache.registerReloadListener();
        }
    }
}