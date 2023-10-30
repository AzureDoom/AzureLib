package mod.azure.azurelib.platform;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.network.S2C_SendConfigData;
import mod.azure.azurelib.network.packet.AnimDataSyncPacket;
import mod.azure.azurelib.network.packet.AnimTriggerPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimTriggerPacket;
import mod.azure.azurelib.network.packet.EntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.EntityAnimTriggerPacket;
import mod.azure.azurelib.network.packet.EntityPacketOnClient;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class FabricAzureLibNetwork implements AzureLibNetwork {

    private void handlePacket(Minecraft client, AbstractPacket packet) {
        client.execute(packet::handle);
    }

    @Override
    public void registerClientReceiverPackets() {
        ClientPlayNetworking.registerGlobalReceiver(ANIM_DATA_SYNC_PACKET_ID, (client, $2, buf, $4) -> this.handlePacket(client, AnimDataSyncPacket.receive(buf)));
        ClientPlayNetworking.registerGlobalReceiver(ANIM_TRIGGER_SYNC_PACKET_ID, (client, $2, buf, $4) ->  this.handlePacket(client, AnimTriggerPacket.receive(buf)));

        ClientPlayNetworking.registerGlobalReceiver(ENTITY_ANIM_DATA_SYNC_PACKET_ID, (client, $2, buf, $4) ->  this.handlePacket(client, EntityAnimDataSyncPacket.receive(buf)));
        ClientPlayNetworking.registerGlobalReceiver(ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID, (client, $2, buf, $4) ->  this.handlePacket(client, EntityAnimTriggerPacket.receive(buf)));

        ClientPlayNetworking.registerGlobalReceiver(BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID, (client, $2, buf, $4) -> this.handlePacket(client, BlockEntityAnimDataSyncPacket.receive(buf)));
        ClientPlayNetworking.registerGlobalReceiver(BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID, (client, $2, buf, $4) -> this.handlePacket(client, BlockEntityAnimTriggerPacket.receive(buf)));

        ClientPlayNetworking.registerGlobalReceiver(CUSTOM_ENTITY_ID, (client, handler, buf, responseSender) -> EntityPacketOnClient.onPacket(client, buf));
    }

    @Override
    public Packet<ClientGamePacketListener> createPacket(Entity entity) {
        FriendlyByteBuf buf = createFriendlyByteBuf();
        buf.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(entity.getType()));
        buf.writeUUID(entity.getUUID());
        buf.writeVarInt(entity.getId());
        buf.writeDouble(entity.getX());
        buf.writeDouble(entity.getY());
        buf.writeDouble(entity.getZ());
        buf.writeByte(Mth.floor(entity.getXRot() * 256.0F / 360.0F));
        buf.writeByte(Mth.floor(entity.getYRot() * 256.0F / 360.0F));
        buf.writeFloat(entity.getXRot());
        buf.writeFloat(entity.getYRot());
        return ServerPlayNetworking.createS2CPacket(AzureLibNetwork.CUSTOM_ENTITY_ID, buf);
    }

    public FriendlyByteBuf createFriendlyByteBuf() {
        return PacketByteBufs.create();
    }

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(entityToTrack)) {
            FriendlyByteBuf buf = createFriendlyByteBuf();
            packet.encode(buf);
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), buf);
        }

        if (entityToTrack instanceof ServerPlayer serverPlayer) {
            FriendlyByteBuf buf = createFriendlyByteBuf();
            packet.encode(buf);
            ServerPlayNetworking.send(serverPlayer, packet.getPacketID(), buf);
        }
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer trackingPlayer : PlayerLookup.tracking(level, blockPos)) {
            FriendlyByteBuf buf = createFriendlyByteBuf();
            packet.encode(buf);
            ServerPlayNetworking.send(trackingPlayer, packet.getPacketID(), buf);
        }
    }

    @Override
    public void sendClientPacket(ServerPlayer player, String id) {
        Networking.sendClientPacket(player, new S2C_SendConfigData(id));
    }
}
