package mod.azure.azurelib.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.packet.*;

/**
 * Network handling class for AzureLib.<br>
 * Handles packet registration and some networking functions
 */
public final class AzureLibNetwork {

    public static final Marker MARKER = MarkerManager.getMarker("Network");

    public static final ResourceLocation CUSTOM_ENTITY_ID = AzureLib.modResource("spawn_entity");

    public static final ResourceLocation AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_blockentity_dispatch_command_sync"
    );

    public static final ResourceLocation AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_entity_dispatch_command_sync"
    );

    public static final ResourceLocation AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = new ResourceLocation(
        AzureLib.MOD_ID,
        "az_item_stack_dispatch_command_sync"
    );

    /**
     * Used to register packets that the server sends
     **/
    public static void registerClientReceiverPackets() {
        ClientPlayNetworking.registerGlobalReceiver(
            CUSTOM_ENTITY_ID,
            (client, handler, buf, responseSender) -> EntityPacketOnClient.onPacket(client, buf)
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzBlockEntityDispatchCommandPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzEntityDispatchCommandPacket::receive
        );

        ClientPlayNetworking.registerGlobalReceiver(
            AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzItemStackDispatchCommandPacket::receive
        );
    }

    public static void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entityToTrack)) {
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), packet.encode());
        }

        if (entityToTrack instanceof ServerPlayer)
            ServerPlayNetworking.send(((ServerPlayer) entityToTrack), packet.getPacketID(), packet.encode());
    }

    public static void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, blockPos)) {
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), packet.encode());
        }
    }
}
