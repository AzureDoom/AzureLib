package mod.azure.azurelib.client;

import net.ornithemc.osl.entrypoints.api.client.ClientModInitializer;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.OrnitheAzureLibNetwork;

/**
 * OSL {@code client-init} entrypoint. Beta has no resource reload listeners, so models and animations are loaded lazily
 * the first time anything asks for them (see {@link mod.azure.azurelib.cache.AzureLibCache#ensureLoaded()}).
 */
public final class AzureLibClientInit implements ClientModInitializer {

    @Override
    public void initClient() {
        AzureLib.initialize();
        OrnitheAzureLibNetwork.registerClientListener();
    }
}
