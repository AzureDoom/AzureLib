package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.util.ClientUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Packet for syncing user-definable animation data for {@link BlockEntity
 * BlockEntities}
 */
public class BlockEntityAnimDataSyncPacket<D> extends AbstractPacket {
	private final BlockPos BLOCK_POS;
	private final SerializableDataTicket<D> DATA_TICKET;
	private final D DATA;

	public BlockEntityAnimDataSyncPacket(BlockPos pos, SerializableDataTicket<D> dataTicket, D data) {
		this.BLOCK_POS = pos;
		this.DATA_TICKET = dataTicket;
		this.DATA = data;
	}

	@Override
	public FriendlyByteBuf encode() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeBlockPos(this.BLOCK_POS);
		buf.writeUtf(this.DATA_TICKET.id());
		this.DATA_TICKET.encode(this.DATA, buf);

		return buf;
	}

	@Override
	public ResourceLocation getPacketID() {
		return AzureLibNetwork.BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID;
	}

	public static <D> void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		final BlockPos BLOCK_POS = buf.readBlockPos();
		final SerializableDataTicket<D> DATA_TICKET = (SerializableDataTicket<D>) DataTickets.byName(buf.readUtf());
		final D DATA = DATA_TICKET.decode(buf);

		client.execute(() -> runOnThread(BLOCK_POS, DATA_TICKET, DATA));
	}

	private static <D> void runOnThread(BlockPos blockPos, SerializableDataTicket<D> dataTicket, D data) {
		BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

		if (blockEntity instanceof GeoBlockEntity geoBlockEntity)
			geoBlockEntity.setAnimData(dataTicket, data);
	}
}
