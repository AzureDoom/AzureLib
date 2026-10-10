package mod.azure.azurelib.network.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.client.ClientUtils;

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

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }

    public static AzBlockEntityDispatchCommandPacket receive(PacketBuffer buf) {
        BlockPos blockPos = buf.readBlockPos(); // Decode block position
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand
        return new AzBlockEntityDispatchCommandPacket(blockPos, dispatchCommand); // Create a new packet instance
    }

    @Override
    public void handle() {
        World level = ClientUtils.getLevel();
        TileEntity blockEntity = level == null ? null : level.getTileEntity(blockPos);

        if (blockEntity == null) {
            return;
        }

        AzAnimator<Object, TileEntity> animator = AzAnimatorAccessor.getOrNull(blockEntity);

        if (animator != null && animator.context().animatable().getPos().equals(blockPos)) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }
}
