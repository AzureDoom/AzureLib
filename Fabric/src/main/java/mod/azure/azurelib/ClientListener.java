package mod.azure.azurelib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.Networking;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
        AzureLibNetwork.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
