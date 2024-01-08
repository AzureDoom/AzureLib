package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.api.common.animatable.GeoBlockEntity;
import mod.azure.azurelib.common.internal.common.constant.DataTickets;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
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
	public void write(FriendlyByteBuf buf) {
		buf.writeBlockPos(this.blockPos);
		buf.writeUtf(this.dataTicket.id());
		this.dataTicket.encode(this.data, buf);
	}

	@Override
	public ResourceLocation id() {
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
