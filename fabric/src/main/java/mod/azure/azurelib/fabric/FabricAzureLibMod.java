package mod.azure.azurelib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.fabric.platform.FabricAzureLibNetwork;

public final class FabricAzureLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        AzureLib.initialize();
        new FabricAzureLibNetwork();
        PayloadTypeRegistry.clientboundPlay()
            .register(
                AzBlockEntityDispatchCommandPacket.TYPE,
                AzBlockEntityDispatchCommandPacket.CODEC
            );
        PayloadTypeRegistry.clientboundPlay()
            .register(
                AzEntityDispatchCommandPacket.TYPE,
                AzEntityDispatchCommandPacket.CODEC
            );
        PayloadTypeRegistry.clientboundPlay()
            .register(
                AzItemStackDispatchCommandPacket.TYPE,
                AzItemStackDispatchCommandPacket.CODEC
            );
    }
}
