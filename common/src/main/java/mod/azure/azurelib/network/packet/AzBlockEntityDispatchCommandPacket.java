package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.rewrite.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.util.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class AzBlockEntityDispatchCommandPacket extends AbstractPacket {

    private final BlockPos blockPos;
    private final AzCommand dispatchCommand;

    public static final StreamCodec<FriendlyByteBuf, AzBlockEntityDispatchCommandPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            AzBlockEntityDispatchCommandPacket::blockPos,
            AzCommand.CODEC,
            AzBlockEntityDispatchCommandPacket::dispatchCommand,
            AzBlockEntityDispatchCommandPacket::new
    );

    public AzBlockEntityDispatchCommandPacket(BlockPos blockPos, AzCommand dispatchCommand) {
        this.blockPos = blockPos;
        this.dispatchCommand = dispatchCommand;
    }

    public BlockPos blockPos() {
        return blockPos;
    }

    public AzCommand dispatchCommand() {
        return dispatchCommand;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        AzCommand.encode(buf, dispatchCommand());
    }

    public static AzBlockEntityDispatchCommandPacket decode(FriendlyByteBuf buf) {
        var blockPos = buf.readBlockPos();

        // Deserialize the AzCommand
        AzCommand command = AzCommand.decode(buf);

        return new AzBlockEntityDispatchCommandPacket(blockPos, dispatchCommand());
    }


    @Override
    public void handle() {
        var blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity == null) {
            return;
        }

        var animator = AzAnimatorAccessor.getOrNull(blockEntity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }
}
