package mod.azure.azurelib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.config.io.ConfigIO;
import mod.azure.azurelib.common.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.common.network.packet.SendConfigDataPacket;
import mod.azure.azurelib.fabric.platform.FabricAzureLibNetwork;

public final class FabricAzureLibMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigIO.FILE_WATCH_MANAGER.startService();
        AzureLib.initialize();
        new FabricAzureLibNetwork();
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> ConfigIO.FILE_WATCH_MANAGER.stopService());
        PayloadTypeRegistry.playS2C()
            .register(
                AzBlockEntityDispatchCommandPacket.TYPE,
                AzBlockEntityDispatchCommandPacket.CODEC
            );
        PayloadTypeRegistry.playS2C()
            .register(
                AzEntityDispatchCommandPacket.TYPE,
                AzEntityDispatchCommandPacket.CODEC
            );
        PayloadTypeRegistry.playS2C()
            .register(
                AzItemStackDispatchCommandPacket.TYPE,
                AzItemStackDispatchCommandPacket.CODEC
            );
        PayloadTypeRegistry.playS2C()
            .register(
                SendConfigDataPacket.TYPE,
                SendConfigDataPacket.CODEC
            );
    }
}
