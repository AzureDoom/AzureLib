package mod.azure.azurelib.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.fabric.platform.FabricAzureLibNetwork;
import mod.azure.azurelib.network.packet.*;

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
        PayloadTypeRegistry.clientboundPlay()
            .register(
                SendConfigDataPacket.TYPE,
                SendConfigDataPacket.CODEC
            );
        PayloadTypeRegistry.serverboundPlay().register(AzHitboxPosePacket.TYPE, AzHitboxPosePacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AzHitboxAttackPacket.TYPE, AzHitboxAttackPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(AzHitboxInteractPacket.TYPE, AzHitboxInteractPacket.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(
            AzHitboxPosePacket.TYPE,
            (packet, context) -> packet.handleServer(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AzHitboxAttackPacket.TYPE,
            (packet, context) -> packet.handleServer(context.player())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            AzHitboxInteractPacket.TYPE,
            (packet, context) -> packet.handleServer(context.player())
        );
    }
}
