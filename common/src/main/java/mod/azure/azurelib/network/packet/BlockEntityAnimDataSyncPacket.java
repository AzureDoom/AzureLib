package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.ClientUtils;
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
	private final BlockPos blockPos;
	private final SerializableDataTicket<D> dataTicket;
	private final D data;

	public BlockEntityAnimDataSyncPacket(BlockPos pos, SerializableDataTicket<D> dataTicket, D data) {
		this.blockPos = pos;
		this.dataTicket = dataTicket;
		this.data = data;
	}

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.blockPos);
		buf.writeUtf(this.dataTicket.id());
		this.dataTicket.encode(this.data, buf);
	}

	@Override
	public ResourceLocation getPacketID() {
		return AzureLibNetwork.BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID;
	}

	public static <D> BlockEntityAnimDataSyncPacket<D> receive(FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		SerializableDataTicket<D> dataTicket = (SerializableDataTicket<D>) DataTickets.byName(buf.readUtf());

		return new BlockEntityAnimDataSyncPacket<>(pos, dataTicket, dataTicket.decode(buf));
	}

	@Override
	public void handle() {
		BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

		if (blockEntity instanceof GeoBlockEntity geoBlockEntity) {
			geoBlockEntity.setAnimData(dataTicket, data);
		}
	}
}
