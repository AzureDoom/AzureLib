package mod.azure.azurelib.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import mod.azure.azurelib.common.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.SendConfigDataPacket;

public final class ClientListener implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(
            AzEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzItemStackDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            AzBlockEntityDispatchCommandPacket.TYPE,
            (packet, context) -> packet.handle()
        );
        ClientPlayNetworking.registerGlobalReceiver(
            SendConfigDataPacket.TYPE,
            (packet, context) -> packet.handle()
        );
    }
}
