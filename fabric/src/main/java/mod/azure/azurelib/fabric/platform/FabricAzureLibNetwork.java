package mod.azure.azurelib.fabric.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.packet.SendConfigDataPacket;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

public class FabricAzureLibNetwork implements AzureLibNetwork {

    public static <B extends FriendlyByteBuf, P extends AbstractPacket> void registerPacket(
        CustomPacketPayload.Type<P> packetType,
        StreamCodec<B, P> codec
    ) {
        PayloadTypeRegistry.playS2C().register(packetType, (StreamCodec<FriendlyByteBuf, P>) codec);
        ClientPlayNetworking.registerGlobalReceiver(packetType, (packet, context) -> packet.handle());
    }

    @Override
    public <B extends FriendlyByteBuf, P extends AbstractPacket> void registerPacketInternal(
        CustomPacketPayload.Type<P> payloadType,
        StreamCodec<B, P> codec,
        boolean isClientBound
    ) {
        if (isClientBound) {
            if (Services.PLATFORM.isEnvironmentClient())
                FabricAzureLibNetwork.registerPacket(payloadType, codec);
        } else {
            PayloadTypeRegistry.playC2S().register(payloadType, (StreamCodec<FriendlyByteBuf, P>) codec);
            ServerPlayNetworking.registerGlobalReceiver(payloadType, (packet, context) -> packet.handle());
        }
    }

    public FriendlyByteBuf createFriendlyByteBuf() {
        return PacketByteBufs.create();
    }

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        if (entityToTrack instanceof ServerPlayer pl)
            sendToPlayer(packet, pl);

        for (ServerPlayer player : PlayerLookup.tracking(entityToTrack)) {
            sendToPlayer(packet, player);
        }
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        for (ServerPlayer player : PlayerLookup.tracking(level, blockPos)) {
            sendToPlayer(packet, player);
        }
    }

    @Override
    public void sendToPlayer(AbstractPacket packet, ServerPlayer player) {
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public void sendClientPacket(ServerPlayer player, String id) {
        ServerPlayNetworking.send(player, new SendConfigDataPacket(id));
    }
}
