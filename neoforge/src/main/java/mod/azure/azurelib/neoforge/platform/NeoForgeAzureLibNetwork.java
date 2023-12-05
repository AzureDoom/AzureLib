package mod.azure.azurelib.neoforge.platform;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.packet.*;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.neoforge.network.Networking;
import mod.azure.azurelib.neoforge.network.ReloadPacket;
import mod.azure.azurelib.neoforge.network.S2C_SendConfigData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkHooks;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.simple.SimpleChannel;

public class NeoForgeAzureLibNetwork implements AzureLibNetwork {
    private static final String VER = "1";
    private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(AzureLib.modResource("main"), () -> VER, VER::equals, VER::equals);

    @Override
    public void reloadGun(int slot) {
        PACKET_CHANNEL.sendToServer(new ReloadPacket(slot));
    }

    @Override
    public Packet<?> createPacket(Entity entity) {
        return NetworkHooks.getEntitySpawningPacket(entity);
    }

    private void handlePacket(AbstractPacket packet, NetworkEvent.Context context) {
        NetworkEvent.Context handler = context;
        context.enqueueWork(packet::handle);
        context.setPacketHandled(true);
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
        PACKET_CHANNEL.registerMessage(id++, ReloadPacket.class, ReloadPacket::encode, ReloadPacket::new, ReloadPacket::handle);
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
