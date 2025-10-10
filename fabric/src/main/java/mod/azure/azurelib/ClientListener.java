package mod.azure.azurelib;

import net.fabricmc.api.ClientModInitializer;

import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
