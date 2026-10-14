package mod.azure.azurelib.client;

import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric {@code client} entrypoint. Beta has no resource reload listeners, so models and animations are loaded lazily
 * the first time anything asks for them (see {@link mod.azure.azurelib.cache.AzureLibCache#ensureLoaded()}).
 */
public final class AzureLibClientInit implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        mod.azure.azurelib.AzureLib.initialize();
    }
}
