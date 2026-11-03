package mod.azure.azurelib;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import mod.azure.azurelib.compat.bettercombat.AzBetterCombatCompat;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.platform.Services;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Services.NETWORK.registerClientReceiverPackets();
        Networking.PacketRegistry.registerClient();
        if (FabricLoader.getInstance().isModLoaded("bettercombat")) {
            AzBetterCombatCompat.init();
        }
    }
}
