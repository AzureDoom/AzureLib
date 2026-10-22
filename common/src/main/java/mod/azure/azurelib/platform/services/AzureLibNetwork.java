package mod.azure.azurelib.platform.services;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzServerboundPacket;

public interface AzureLibNetwork {

    ResourceLocation CUSTOM_ENTITY_ID = AzureLib.modResource("spawn_entity");

    ResourceLocation AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_blockentity_dispatch_command_sync"
    );

    ResourceLocation AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_entity_dispatch_command_sync"
    );

    ResourceLocation AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID = AzureLib.modResource(
        "az_item_stack_dispatch_command_sync"
    );

    ResourceLocation AZ_HITBOX_POSE_PACKET_ID = AzureLib.modResource("az_hitbox_pose");

    ResourceLocation AZ_HITBOX_ATTACK_PACKET_ID = AzureLib.modResource("az_hitbox_attack");

    ResourceLocation AZ_HITBOX_INTERACT_PACKET_ID = AzureLib.modResource("az_hitbox_interact");

    Packet<ClientGamePacketListener> createPacket(Entity entity);

    void registerClientReceiverPackets();

    void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack);

    void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos);

    void sendClientPacket(ServerPlayer player, String id);

    static void sendWithCallback(AbstractPacket packet, IPacketCallback callback) {
        callback.onReadyToSend(packet);
    }

    <P extends AzServerboundPacket> void registerServerbound(
        ResourceLocation id,
        Class<P> type,
        Function<FriendlyByteBuf, P> decoder
    );

    void sendToServer(AzServerboundPacket packet);

    interface IPacketCallback {

        void onReadyToSend(AbstractPacket packetToSend);
    }
}
