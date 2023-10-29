package mod.azure.azurelib.platform;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.packet.AnimDataSyncPacket;
import mod.azure.azurelib.network.packet.AnimTriggerPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.BlockEntityAnimTriggerPacket;
import mod.azure.azurelib.network.packet.EntityAnimDataSyncPacket;
import mod.azure.azurelib.network.packet.EntityAnimTriggerPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class NeoForgeAzureLibNetwork implements AzureLibNetwork {
    private static final String VER = "1";
    private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(AzureLib.MOD_ID, "main"), () -> VER, VER::equals, VER::equals);

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

        PACKET_CHANNEL.registerMessage(id++, AnimDataSyncPacket.class, AnimDataSyncPacket::encode, AnimDataSyncPacket::receive, this::handlePacket);
        PACKET_CHANNEL.registerMessage(id++, AnimTriggerPacket.class, AnimTriggerPacket::encode, AnimTriggerPacket::receive, this::handlePacket);
        PACKET_CHANNEL.registerMessage(id++, EntityAnimDataSyncPacket.class, EntityAnimDataSyncPacket::encode, EntityAnimDataSyncPacket::receive, this::handlePacket);
        PACKET_CHANNEL.registerMessage(id++, EntityAnimTriggerPacket.class, EntityAnimTriggerPacket::encode, EntityAnimTriggerPacket::receive, this::handlePacket);
        PACKET_CHANNEL.registerMessage(id++, BlockEntityAnimDataSyncPacket.class, BlockEntityAnimDataSyncPacket::encode, BlockEntityAnimDataSyncPacket::receive, this::handlePacket);
        PACKET_CHANNEL.registerMessage(id++, BlockEntityAnimTriggerPacket.class, BlockEntityAnimTriggerPacket::encode, BlockEntityAnimTriggerPacket::receive, this::handlePacket);
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
}
