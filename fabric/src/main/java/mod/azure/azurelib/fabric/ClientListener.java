package mod.azure.azurelib.fabric;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.fabric.network.Networking;
import mod.azure.azurelib.common.platform.Services;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(ClientUtils.RELOAD);
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
    }
}
