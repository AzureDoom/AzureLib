package mod.azure.azurelib.network.packet;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.util.ClientUtils;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntities}
 */
public class BlockEntityAnimTriggerPacket<D> {
	private final BlockPos pos;
	private final String controllerName;
	private final String animName;

	public BlockEntityAnimTriggerPacket(BlockPos pos, @Nullable String controllerName, String animName) {
		this.pos = pos;
		this.controllerName = controllerName == null ? "" : controllerName;
		this.animName = animName;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeBlockPos(this.pos);
		buffer.writeUtf(this.controllerName);
		buffer.writeUtf(this.animName);
	}

	public static <D> BlockEntityAnimTriggerPacket<D> decode(PacketBuffer buffer) {
		return new BlockEntityAnimTriggerPacket<>(buffer.readBlockPos(), buffer.readUtf(), buffer.readUtf());
	}

	public void receivePacket(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context handler = context.get();

		handler.enqueueWork(() -> {
			TileEntity blockEntity = ClientUtils.getLevel().getBlockEntity(this.pos);

			if (blockEntity instanceof GeoBlockEntity)
				((GeoBlockEntity) blockEntity).triggerAnim(this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
		});
		handler.setPacketHandled(true);
	}
}
