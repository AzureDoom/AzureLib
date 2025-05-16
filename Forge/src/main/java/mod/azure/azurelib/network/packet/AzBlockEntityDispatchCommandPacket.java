package mod.azure.azurelib.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.util.ClientUtils;

public class AzBlockEntityDispatchCommandPacket extends AbstractPacket {

    private final BlockPos blockPos;

    private final AzCommand dispatchCommand;

    public AzBlockEntityDispatchCommandPacket(
        BlockPos blockPos,
        AzCommand dispatchCommand
    ) {
        this.blockPos = blockPos;
        this.dispatchCommand = dispatchCommand;
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeBlockPos(this.blockPos);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
    }

    public static AzBlockEntityDispatchCommandPacket receive(PacketBuffer buf) {
        BlockPos blockPos = buf.readBlockPos(); // Decode block position
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzBlockEntityDispatchCommandPacket(blockPos, dispatchCommand); // Create a new packet instance
    }

    @Override
    public void handle() {
        TileEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity == null) {
            return;
        }

        AzAnimator<TileEntity> animator = AzAnimatorAccessor.getOrNull(blockEntity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
