package mod.azure.azurelib;

import net.fabricmc.api.ClientModInitializer;

import mod.azure.azurelib.network.AzureLibNetwork;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        AzureLibNetwork.registerClientReceiverPackets();
    }
}
