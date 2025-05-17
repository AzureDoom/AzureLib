package mod.azure.azurelib.network.packet;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.dispatch.AzDispatchSide;
import mod.azure.azurelib.animation.dispatch.command.AzCommand;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzureLibNetwork;
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
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeBlockPos(this.blockPos);
        AzCommand.ENCODER.accept(buf, this.dispatchCommand);
        return buf;
    }

    public static void receive(
        Minecraft client,
        ClientPacketListener handler,
        FriendlyByteBuf buf,
        PacketSender responseSender
    ) {
        BlockPos blockPos = buf.readBlockPos(); // Decode block position
        AzCommand dispatchCommand = AzCommand.DECODER.apply(buf); // Decode AzCommand

        client.execute(() -> runOnThread(blockPos, dispatchCommand));
    }

    private static void runOnThread(BlockPos blockPos, AzCommand dispatchCommand) {
        BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        AzAnimator<BlockEntity> animator = AzAnimatorAccessor.getOrNull(blockEntity);

        if (animator != null) {
            dispatchCommand.actions().forEach(action -> action.handle(AzDispatchSide.SERVER, animator));
        }
    }

    @Override
    public ResourceLocation getPacketID() {
        return AzureLibNetwork.AZ_BLOCKENTITY_DISPATCH_COMMAND_SYNC_PACKET_ID;
    }
}
