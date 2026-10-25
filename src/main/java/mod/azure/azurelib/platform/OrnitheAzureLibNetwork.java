package mod.azure.azurelib.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.server.entity.mob.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.ornithemc.osl.core.api.util.NamespacedIdentifier;
import net.ornithemc.osl.networking.api.ChannelIdentifiers;
import net.ornithemc.osl.networking.api.ChannelRegistry;
import net.ornithemc.osl.networking.api.PacketBuffer;
import net.ornithemc.osl.networking.api.PacketPayload;
import net.ornithemc.osl.networking.api.client.ClientPlayNetworking;
import net.ornithemc.osl.networking.api.server.ServerPlayNetworking;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.AzServerboundPacket;
import mod.azure.azurelib.network.AzServerboundPackets;
import mod.azure.azurelib.network.ByteBuf;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.math.BlockPos;

/**
 * Beta networking through OSL's Networking API.
 * <p>
 * Every AzureLib server-to-client packet travels as a single {@link RawPayload} on the {@code azurelib:packet} channel.
 * The payload holds a one-byte AzureLib packet id followed by the packet's own encoding, so AzureLib's packet classes
 * don't depend on OSL at all; this is the only class that touches OSL networking.
 * <p>
 * Beta single player has no integrated server: the client runs the world itself. When the "server side" of a dispatch
 * is that local world, packets are encoded and decoded in place and handled immediately, so single player and
 * multiplayer go through exactly the same code.
 */
public class OrnitheAzureLibNetwork implements AzureLibNetwork {

    public static final String CHANNEL_PATH = "packet";

    /** Client-to-server channel (hitbox poses and melee/interact claims). Only the server ever receives on it. */
    public static final String SERVERBOUND_CHANNEL_PATH = "serverbound";

    private static final List<Function<AzByteBuf, ? extends AzServerboundPacket>> SERVERBOUND_DECODERS =
        new ArrayList<>();

    private static final Map<ResourceLocation, Integer> SERVERBOUND_IDS = new HashMap<>();

    private static NamespacedIdentifier serverboundChannel;

    private static final List<Function<AzByteBuf, ? extends AbstractPacket>> DECODERS = new ArrayList<>();

    private static final Map<ResourceLocation, Integer> IDS = new HashMap<>();

    /**
     * Beta's server has no "players tracking this entity" query that AzureLib can reach without an accessor, so packets
     * go to every player in the world within this many blocks, which covers vanilla tracking ranges.
     */
    public static double trackingRange = 128.0D;

    private static NamespacedIdentifier channel;

    private boolean registered;

    /** Registers the {@code azurelib:packet} channel with OSL. Called from the common entrypoint on both sides. */
    public static synchronized void registerChannel() {
        if (channel == null) {
            channel = ChannelRegistry.register(ChannelIdentifiers.from(AzureLib.MOD_ID, CHANNEL_PATH));
        }
        if (serverboundChannel == null) {
            serverboundChannel = ChannelRegistry.register(
                ChannelIdentifiers.from(AzureLib.MOD_ID, SERVERBOUND_CHANNEL_PATH)
            );
        }
    }

    public static NamespacedIdentifier serverboundChannel() {
        registerChannel();
        return serverboundChannel;
    }

    /**
     * Dedicated server side: decode and run the packets clients send. Called from the common entrypoint on a dedicated
     * server only (single player handles these in place, see {@link #sendToServer}).
     */
    public static void registerServerListener() {
        ServerReceiver.register();
    }

    public static NamespacedIdentifier channel() {
        registerChannel();
        return channel;
    }

    /** Client side: decode and run AzureLib packets on the client thread. Called from the client entrypoint. */
    public static void registerClientListener() {
        ClientPlayNetworking.registerListener(channel(), RawPayload::new, (context, payload) -> {
            context.ensureOnMainThread();
            receive(payload.bytes);
        });
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
        // Both sides register in the same order, so the one-byte ids line up.
        AzServerboundPackets.register(this);
    }

    @Override
    public <P extends AzServerboundPacket> void registerServerbound(
        ResourceLocation id,
        Class<P> type,
        Function<AzByteBuf, P> decoder
    ) {
        if (SERVERBOUND_IDS.containsKey(id)) {
            return;
        }
        SERVERBOUND_IDS.put(id, SERVERBOUND_DECODERS.size());
        SERVERBOUND_DECODERS.add(decoder);
    }

    /**
     * Sends a packet to the server. In single player there is no server to send to and the client world is the
     * authoritative one, so the packet is handled immediately with the local player as its sender.
     * <p>
     * Client only: callers are client hooks.
     */
    @Override
    public void sendToServer(AzServerboundPacket packet) {
        PlayerEntity player = mod.azure.azurelib.platform.mc.McClient.player();
        if (player == null) {
            return;
        }

        if (!Mc.isClientSide(Mc.world(player))) {
            packet.handleServer(player);
            return;
        }

        Integer id = SERVERBOUND_IDS.get(packet.id());
        if (id == null) {
            throw new IllegalStateException("Unregistered AzureLib serverbound packet " + packet.id());
        }
        ByteBuf raw = new ByteBuf();
        AzByteBuf buffer = new AzByteBuf(raw);
        buffer.writeByte(id);
        packet.write(buffer);

        ClientPlayNetworking.send(serverboundChannel(), new RawPayload(raw.toByteArray()));
    }

    /** Decodes and handles a packet a client sent. Runs on the server thread. */
    static void receiveServerbound(PlayerEntity sender, byte[] bytes) {
        if (sender == null || bytes == null || bytes.length == 0) {
            return;
        }
        AzByteBuf buffer = new AzByteBuf(ByteBuf.wrap(bytes));
        int id = buffer.readUnsignedByte();
        if (id >= SERVERBOUND_DECODERS.size()) {
            AzureLib.LOGGER.warn("Unknown AzureLib serverbound packet id {} from entity {}", id, Mc.entityId(sender));
            return;
        }
        try {
            AzServerboundPacket packet = SERVERBOUND_DECODERS.get(id).apply(buffer);
            if (packet != null) {
                packet.handleServer(sender);
            }
        } catch (RuntimeException e) {
            // Client-controlled bytes: log and drop instead of letting a bad packet kick anyone.
            AzureLib.LOGGER.warn(
                "Dropped malformed AzureLib serverbound packet {} from entity {}",
                id,
                Mc.entityId(sender),
                e
            );
        }
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

        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            if (!Mc.isClientSide(world)) {
                // Single player: this world *is* the client world.
                receive(bytes);
            }
            // A multiplayer client world has nobody to send to.
            return;
        }

        ServerSender.sendAround(bytes, world, x, y, z, trackingRange);
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

    /**
     * Dedicated-server-only sending. Kept in its own class so the server player class is never resolved on the client,
     * where it doesn't exist.
     */
    private static final class ServerSender {

        private ServerSender() {}

        static void sendAround(byte[] bytes, World world, double x, double y, double z, double range) {
            double rangeSq = range * range;
            RawPayload payload = new RawPayload(bytes);
            for (PlayerEntity player : Mc.players(world)) {
                if (!(player instanceof ServerPlayerEntity)) {
                    continue;
                }
                double dx = Mc.x(player) - x;
                double dy = Mc.y(player) - y;
                double dz = Mc.z(player) - z;
                if (dx * dx + dy * dy + dz * dz <= rangeSq) {
                    ServerPlayNetworking.send((ServerPlayerEntity) player, channel(), payload);
                }
            }
        }
    }

    /**
     * Dedicated-server-only receiving, kept apart for the same reason as {@link ServerSender}.
     */
    private static final class ServerReceiver {

        private ServerReceiver() {}

        static void register() {
            ServerPlayNetworking.registerListener(serverboundChannel(), RawPayload::new, (context, payload) -> {
                context.ensureOnMainThread();
                receiveServerbound(context.player(), payload.bytes);
            });
        }
    }

    /**
     * Opaque OSL payload carrying one already-encoded AzureLib packet. Length-prefixed so it doesn't rely on knowing
     * how many bytes remain in the buffer.
     */
    public static final class RawPayload implements PacketPayload {

        private byte[] bytes;

        public RawPayload() {
            this(new byte[0]);
        }

        public RawPayload(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public void read(PacketBuffer buffer) throws IOException {
            int length = buffer.readInt();
            if (length < 0 || length > 1_048_576) {
                throw new IOException("Bad AzureLib payload length " + length);
            }
            this.bytes = new byte[length];
            buffer.readBytes(this.bytes);
        }

        @Override
        public void write(PacketBuffer buffer) throws IOException {
            buffer.writeInt(this.bytes.length);
            buffer.writeBytes(this.bytes);
        }
    }
}
