package mod.azure.azurelib.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import net.modificationstation.stationapi.api.network.packet.PacketHelper;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.ByteBuf;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.math.BlockPos;

/**
 * Beta networking through StationAPI.
 * <p>
 * Every AzureLib server-to-client packet travels as a single StationAPI {@link MessagePacket} on the
 * {@code azurelib:packet} channel, whose {@code bytes} hold a one-byte AzureLib packet id followed by the packet's own
 * encoding. The client listener is registered by {@link mod.azure.azurelib.platform.AzureLibStationEvents}.
 * <p>
 * Beta single player has no integrated server: the client runs the world itself. When the "server side" of a dispatch
 * is that local world, packets are encoded and decoded in place and handled immediately, so single player and
 * multiplayer go through exactly the same code.
 */
public class StationAzureLibNetwork implements AzureLibNetwork {

    public static final String CHANNEL = "packet";

    private static final List<Function<AzByteBuf, ? extends AbstractPacket>> DECODERS = new ArrayList<>();

    private static final Map<ResourceLocation, Integer> IDS = new HashMap<>();

    /**
     * Beta's server has no "players tracking this entity" query that AzureLib can reach without an accessor, so packets
     * go to every player in the world within this many blocks, which covers vanilla tracking ranges.
     */
    public static double trackingRange = 128.0D;

    private boolean registered;

    public static Identifier channelId() {
        return Identifier.of(Namespace.of(AzureLib.MOD_ID), CHANNEL);
    }

    private static void registerPacket(ResourceLocation id, Function<AzByteBuf, ? extends AbstractPacket> decoder) {
        IDS.put(id, DECODERS.size());
        DECODERS.add(decoder);
    }

    @Override
    public void registerClientReceiverPackets() {
        if (this.registered) {
            return;
        }
        this.registered = true;
        registerPacket(
            AzureLibNetwork.AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzBlockEntityDispatchCommandPacket::receive
        );
        registerPacket(
            AzureLibNetwork.AZ_ENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzEntityDispatchCommandPacket::receive
        );
        registerPacket(
            AzureLibNetwork.AZ_ITEM_STACK_DISPATCH_COMMAND_SYNC_PACKET_ID,
            AzItemStackDispatchCommandPacket::receive
        );
    }

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        sendAround(packet, Mc.world(entityToTrack), Mc.x(entityToTrack), Mc.y(entityToTrack), Mc.z(entityToTrack));
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, World level, BlockPos blockPos) {
        sendAround(packet, level, blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
    }

    private void sendAround(AbstractPacket packet, World world, double x, double y, double z) {
        if (world == null) {
            return;
        }
        byte[] bytes = encode(packet);

        if (isLocalClientWorld(world)) {
            // Single player: this world *is* the client world.
            receive(bytes);
            return;
        }

        double rangeSq = trackingRange * trackingRange;
        for (PlayerEntity player : Mc.players(world)) {
            double dx = Mc.x(player) - x;
            double dy = Mc.y(player) - y;
            double dz = Mc.z(player) - z;
            if (dx * dx + dy * dy + dz * dz <= rangeSq) {
                MessagePacket message = new MessagePacket(channelId());
                message.bytes = bytes;
                PacketHelper.sendTo(player, message);
            }
        }
    }

    private static boolean isLocalClientWorld(World world) {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT && !Mc.isClientSide(world);
    }

    private static byte[] encode(AbstractPacket packet) {
        Integer id = IDS.get(packet.getPacketID());
        if (id == null) {
            throw new IllegalStateException("Unregistered AzureLib packet " + packet.getPacketID());
        }
        ByteBuf raw = new ByteBuf();
        AzByteBuf buffer = new AzByteBuf(raw);
        buffer.writeByte(id);
        packet.encode(buffer);
        return raw.toByteArray();
    }

    /** Decodes and handles a packet received from the server. Runs on the client thread. */
    public static void receive(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        AzByteBuf buffer = new AzByteBuf(ByteBuf.wrap(bytes));
        int id = buffer.readUnsignedByte();
        if (id >= DECODERS.size()) {
            AzureLib.LOGGER.error("Unknown AzureLib packet id {}", id);
            return;
        }
        try {
            AbstractPacket packet = DECODERS.get(id).apply(buffer);
            if (packet != null) {
                packet.handle();
            }
        } catch (RuntimeException e) {
            AzureLib.LOGGER.error("Failed to handle AzureLib packet {}", id, e);
        }
    }
}
