package mod.azure.azurelib.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.platform.services.AzureLibNetwork;
import mod.azure.azurelib.util.client.ClientUtils;

public record AzBlockEntityDispatchCommandPacket(
    BlockPos blockPos,
    AzCommand dispatchCommand
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<AzBlockEntityDispatchCommandPacket> TYPE = new Type<>(
        AzureLibNetwork.AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, AzBlockEntityDispatchCommandPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        AzBlockEntityDispatchCommandPacket::blockPos,
        AzCommand.CODEC,
        AzBlockEntityDispatchCommandPacket::dispatchCommand,
        AzBlockEntityDispatchCommandPacket::new
    );

    @Override
    public void handle() {
        var blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity == null) {
            return;
        }

        var animator = AzAnimatorAccessor.getOrNull(blockEntity);

        if (animator != null && animator.context().animatable().getBlockPos().equals(blockPos)) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
