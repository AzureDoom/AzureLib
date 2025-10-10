package mod.azure.azurelib.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.network.S2C_SendConfigData;
import mod.azure.azurelib.network.packet.*;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public class NeoForgeAzureLibNetwork implements AzureLibNetwork {

    private static final String VER = "1";

    private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(
        AzureLib.modResource("main"),
        () -> VER,
        VER::equals,
        VER::equals
    );

    @Override
    public Packet<ClientGamePacketListener> createPacket(Entity entity) {
        return NetworkHooks.getEntitySpawningPacket(entity);
    }

    private void handlePacket(AbstractPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();
        handler.enqueueWork(packet::handle);
        handler.setPacketHandled(true);
    }

    @Override
    public void registerClientReceiverPackets() {
        int id = 0;

        PACKET_CHANNEL.registerMessage(
            id++,
            AzBlockEntityDispatchCommandPacket.class,
            AzBlockEntityDispatchCommandPacket::encode,
            AzBlockEntityDispatchCommandPacket::receive,
            this::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            AzEntityDispatchCommandPacket.class,
            AzEntityDispatchCommandPacket::encode,
            AzEntityDispatchCommandPacket::receive,
            this::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            id++,
            AzItemStackDispatchCommandPacket.class,
            AzItemStackDispatchCommandPacket::encode,
            AzItemStackDispatchCommandPacket::receive,
            this::handlePacket
        );
    }

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entityToTrack));
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        send(packet, PacketDistributor.TRACKING_CHUNK.with(() -> level.getChunkAt(blockPos)));
    }

    /**
     * Send a packet using AzureLib's packet channel
     */
    public static <M> void send(M packet, PacketDistributor.PacketTarget distributor) {
        PACKET_CHANNEL.send(distributor, packet);
    }

    @Override
    public void sendClientPacket(ServerPlayer player, String id) {
        Networking.sendClientPacket(player, new S2C_SendConfigData(id));
    }
}
