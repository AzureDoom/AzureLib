package mod.azure.azurelib.platform.services;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.util.math.BlockPos;

public interface AzureLibNetwork {

    ResourceLocation AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_blockentity_dispatch_command_sync"
    );

    ResourceLocation AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_entity_dispatch_command_sync"
    );

    ResourceLocation AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_item_stack_dispatch_command_sync"
    );

    /**
     * Used to register packets that the server sends
     **/
    void registerClientReceiverPackets();

    void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack);

    void sendToEntitiesTrackingChunk(AbstractPacket packet, World level, BlockPos blockPos);

    static void sendWithCallback(AbstractPacket packet, IPacketCallback callback) {
        callback.onReadyToSend(packet);
    }

    interface IPacketCallback {

        void onReadyToSend(AbstractPacket packetToSend);
    }
}
