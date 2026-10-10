package mod.azure.azurelib.platform;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzByteBuf;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.math.BlockPos;

/**
 * 1.7.10 networking. All of AzureLib's server-to-client packets travel through a single {@link SimpleNetworkWrapper}
 * message, {@link AzPacketMessage}, which carries a one-byte packet id followed by the packet's own encoding.
 */
public class ForgeAzureLibNetwork implements AzureLibNetwork {

    private static final SimpleNetworkWrapper PACKET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(
        AzureLib.MOD_ID
    );

    private static final List<Function<AzByteBuf, ? extends AbstractPacket>> DECODERS = new ArrayList<>();

    private static final Map<ResourceLocation, Integer> IDS = new HashMap<>();

    private boolean registered;

    private static void registerPacket(ResourceLocation id, Function<AzByteBuf, ? extends AbstractPacket> decoder) {
        IDS.put(id, DECODERS.size());
        DECODERS.add(decoder);
    }

    @Override
    public void registerClientReceiverPackets() {
        if (registered) {
            return;
        }
        registered = true;
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
        PACKET_CHANNEL.registerMessage(AzPacketHandler.class, AzPacketMessage.class, 0, Side.CLIENT);
    }

    /**
     * 1.7.10's {@link SimpleNetworkWrapper} has no "send to tracking" target, so packets go to every player within this
     * many blocks, which covers the default entity tracking ranges.
     */
    public static double trackingRange = 128.0D;

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        AzPacketMessage message = new AzPacketMessage(packet);
        PACKET_CHANNEL.sendToAllAround(
            message,
            new NetworkRegistry.TargetPoint(
                entityToTrack.dimension,
                entityToTrack.posX,
                entityToTrack.posY,
                entityToTrack.posZ,
                trackingRange
            )
        );
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, World level, BlockPos blockPos) {
        PACKET_CHANNEL.sendToAllAround(
            new AzPacketMessage(packet),
            new NetworkRegistry.TargetPoint(
                level.provider.dimensionId,
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D,
                trackingRange
            )
        );
    }

    /**
     * Wrapper message for every AzureLib packet. Must keep a public no-arg constructor for Forge.
     */
    public static class AzPacketMessage implements IMessage {

        private AbstractPacket packet;

        public AzPacketMessage() {}

        public AzPacketMessage(AbstractPacket packet) {
            this.packet = packet;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            AzByteBuf buffer = new AzByteBuf(buf);
            int id = buffer.readUnsignedByte();
            if (id < 0 || id >= DECODERS.size()) {
                throw new IllegalStateException("Unknown AzureLib packet id " + id);
            }
            this.packet = DECODERS.get(id).apply(buffer);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            AzByteBuf buffer = new AzByteBuf(buf);
            Integer id = IDS.get(this.packet.getPacketID());
            if (id == null) {
                throw new IllegalStateException("Unregistered AzureLib packet " + this.packet.getPacketID());
            }
            buffer.writeByte(id);
            this.packet.encode(buffer);
        }
    }

    public static class AzPacketHandler implements IMessageHandler<AzPacketMessage, IMessage> {

        @Override
        public IMessage onMessage(AzPacketMessage message, MessageContext ctx) {
            // On 1.7.10 custom payloads are processed on the client thread, so the packet can be handled directly.
            if (message.packet != null) {
                message.packet.handle();
            }
            return null;
        }
    }
}
