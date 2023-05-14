package mod.azure.azurelib;

import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.Networking;
import net.fabricmc.api.ClientModInitializer;

public final class ClientListener implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		AzureLibNetwork.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
	}
}
