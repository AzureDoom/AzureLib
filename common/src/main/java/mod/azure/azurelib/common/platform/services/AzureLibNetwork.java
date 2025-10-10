package mod.azure.azurelib.common.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.network.AbstractPacket;

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

    ResourceLocation CONFIG_PACKET_ID = AzureLib.modResource("config_packet");

    static void sendWithCallback(AbstractPacket packet, IPacketCallback callback) {
        callback.onReadyToSend(packet);
    }

    <B extends FriendlyByteBuf, P extends AbstractPacket> void registerPacketInternal(
        CustomPacketPayload.Type<P> payloadType,
        StreamCodec<B, P> codec,
        boolean isClientBound
    );

    void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack);

    void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos);

    void sendClientPacket(ServerPlayer player, String id);

    void sendToPlayer(AbstractPacket packet, ServerPlayer player);

    interface IPacketCallback {

        void onReadyToSend(AbstractPacket packetToSend);
    }
}
