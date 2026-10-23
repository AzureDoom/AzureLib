package mod.azure.azurelib.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.network.Networking;
import mod.azure.azurelib.network.S2C_SendConfigData;
import mod.azure.azurelib.network.packet.*;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

public class ForgeAzureLibNetwork implements AzureLibNetwork {

    private static final String VER = "1";

    private static int nextMessageId = 0;

    private static final SimpleChannel PACKET_CHANNEL = NetworkRegistry.newSimpleChannel(
        AzureLib.modResource("main"),
        () -> VER,
        VER::equals,
        VER::equals
    );

    @Override
    public Packet<ClientGamePacketListener> createPacket(Entity entity) {
        return (Packet<ClientGamePacketListener>) NetworkHooks.getEntitySpawningPacket(entity);
    }

    private void handlePacket(AbstractPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();
        handler.enqueueWork(packet::handle);
        handler.setPacketHandled(true);
    }

    @Override
    public void registerClientReceiverPackets() {
        PACKET_CHANNEL.registerMessage(
            nextMessageId++,
            AzBlockEntityDispatchCommandPacket.class,
            AzBlockEntityDispatchCommandPacket::encode,
            AzBlockEntityDispatchCommandPacket::receive,
            this::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            nextMessageId++,
            AzEntityDispatchCommandPacket.class,
            AzEntityDispatchCommandPacket::encode,
            AzEntityDispatchCommandPacket::receive,
            this::handlePacket
        );
        PACKET_CHANNEL.registerMessage(
            nextMessageId++,
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

    @Override
    public <P extends AzServerboundPacket> void registerServerbound(
        ResourceLocation id,
        Class<P> type,
        Function<FriendlyByteBuf, P> decoder
    ) {
        PACKET_CHANNEL.messageBuilder(type, nextMessageId++, NetworkDirection.PLAY_TO_SERVER)
            .encoder(AzServerboundPacket::write)
            .decoder(decoder)
            .consumer((packet, context) -> {
                NetworkEvent.Context ctx = context.get();

                ctx.enqueueWork(() -> {
                    var sender = ctx.getSender();

                    if (sender != null) {
                        packet.handleServer(sender);
                    }
                });

                ctx.setPacketHandled(true);
            })
            .add();
    }

    @Override
    public void sendToServer(AzServerboundPacket packet) {
        PACKET_CHANNEL.sendToServer(packet);
    }
}
