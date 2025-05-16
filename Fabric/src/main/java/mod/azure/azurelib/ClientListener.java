package mod.azure.azurelib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

import mod.azure.azurelib.network.AzureLibNetwork;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(Keybindings.RELOAD);
        AzureLibNetwork.registerClientReceiverPackets();
        AzureLibNetwork.PacketRegistry.registerClient();
    }
}
