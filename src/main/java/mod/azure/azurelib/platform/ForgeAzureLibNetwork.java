package mod.azure.azurelib.platform;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.packet.AzBlockEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzEntityDispatchCommandPacket;
import mod.azure.azurelib.network.packet.AzItemStackDispatchCommandPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;

/**
 * 1.12.2 networking. All of AzureLib's server-to-client packets travel through a single {@link SimpleNetworkWrapper}
 * message, {@link AzPacketMessage}, which carries a one-byte packet id followed by the packet's own encoding.
 */
public class ForgeAzureLibNetwork implements AzureLibNetwork {

    private static final SimpleNetworkWrapper PACKET_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(
        AzureLib.MOD_ID
    );

    private static final List<Function<PacketBuffer, ? extends AbstractPacket>> DECODERS = new ArrayList<>();

    private static final Map<ResourceLocation, Integer> IDS = new HashMap<>();

    private boolean registered;

    private static void registerPacket(ResourceLocation id, Function<PacketBuffer, ? extends AbstractPacket> decoder) {
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

    @Override
    public void sendToTrackingEntityAndSelf(AbstractPacket packet, Entity entityToTrack) {
        AzPacketMessage message = new AzPacketMessage(packet);
        PACKET_CHANNEL.sendToAllTracking(message, entityToTrack);
        if (entityToTrack instanceof EntityPlayerMP) {
            PACKET_CHANNEL.sendTo(message, (EntityPlayerMP) entityToTrack);
        }
    }

    @Override
    public void sendToEntitiesTrackingChunk(AbstractPacket packet, World level, BlockPos blockPos) {
        PACKET_CHANNEL.sendToAllTracking(
            new AzPacketMessage(packet),
            new NetworkRegistry.TargetPoint(
                level.provider.getDimension(),
                blockPos.getX() + 0.5D,
                blockPos.getY() + 0.5D,
                blockPos.getZ() + 0.5D,
                0.0D
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
            PacketBuffer buffer = new PacketBuffer(buf);
            int id = buffer.readUnsignedByte();
            if (id < 0 || id >= DECODERS.size()) {
                throw new IllegalStateException("Unknown AzureLib packet id " + id);
            }
            this.packet = DECODERS.get(id).apply(buffer);
        }

        @Override
        public void toBytes(ByteBuf buf) {
            PacketBuffer buffer = new PacketBuffer(buf);
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
            final AbstractPacket packet = message.packet;
            if (packet != null) {
                FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(packet::handle);
            }
            return null;
        }
    }
}
