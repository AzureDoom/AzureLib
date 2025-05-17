/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import mod.azure.azurelib.animatable.GeoBlockEntity;
import mod.azure.azurelib.util.ClientUtils;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for
 * {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntities}
 */
@Deprecated()
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
        buffer.writeString(this.controllerName);
        buffer.writeString(this.animName);
    }

    public static <D> BlockEntityAnimTriggerPacket<D> decode(PacketBuffer buffer) {
        return new BlockEntityAnimTriggerPacket<>(buffer.readBlockPos(), buffer.readString(), buffer.readString());
    }

    public void receivePacket(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();

        handler.enqueueWork(() -> {
            TileEntity blockEntity = ClientUtils.getLevel().getTileEntity(this.pos);

            if (blockEntity instanceof GeoBlockEntity)
                ((GeoBlockEntity) blockEntity).triggerAnim(
                    this.controllerName.isEmpty() ? null : this.controllerName,
                    this.animName
                );
        });
        handler.setPacketHandled(true);
    }
}
