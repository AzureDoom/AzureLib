package mod.azure.azurelib.neoforge.platform;

import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.neoforge.network.S2C_NeoSendConfigData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public class NeoForgeAzureLibNetwork implements AzureLibNetwork {

    @Override
    public Packet<?> createPacket(Entity entity) {
        return entity.getAddEntityPacket();
    }

    public static void handlePacket(AbstractPacket packet, PlayPayloadContext context) {
        context.workHandler().execute(packet::handle);
    }

    @Override
    public void registerClientReceiverPackets() {
    }

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        send(packet, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entityToTrack));
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, ServerLevel level, BlockPos blockPos) {
        send(packet, PacketDistributor.TRACKING_CHUNK.with(level.getChunkAt(blockPos)));
    }

    /**
     * Send a packet using AzureLib's packet channel
     */
    public static <M> void send(M packet, PacketDistributor.PacketTarget distributor) {
        distributor.send((CustomPacketPayload) packet);
    }

    @Override
    public void sendClientPacket(ServerPlayer player, String id) {
        send(new S2C_NeoSendConfigData(id), PacketDistributor.PLAYER.with(player));
    }
}
