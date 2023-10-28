package mod.azure.azurelib;

import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class ClientListener implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
		Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
	}
}
