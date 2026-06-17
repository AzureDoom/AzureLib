package mod.azure.azurelib.neoforge.platform;

import net.neoforged.fml.loading.FMLEnvironment;

import mod.azure.azurelib.cache.AzureLibCache;
import mod.azure.azurelib.platform.services.AzureLibInitializer;

public class NeoForgeAzureLibInitializer implements AzureLibInitializer {

    @Override
    public void initialize() {
        if (FMLEnvironment.getDist().isClient()) {
            AzureLibCache.registerReloadListener();
        }
    }
}
