package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.api.common.animatable.GeoBlockEntity;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Packet for syncing user-definable animations that can be triggered from the
 * server for {@link net.minecraft.world.level.block.entity.BlockEntity
 * BlockEntities}
 */
public class BlockEntityAnimTriggerPacket extends AbstractPacket {
    private final BlockPos blockPos;
    private final String controllerName;
    private final String animName;

    public BlockEntityAnimTriggerPacket(BlockPos blockPos, @Nullable String controllerName, String animName) {
        this.blockPos = blockPos;
        this.controllerName = controllerName == null ? "" : controllerName;
        this.animName = animName;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.blockPos);
        buf.writeUtf(this.controllerName);
        buf.writeUtf(this.animName);
    }

    @Override
    public ResourceLocation id() {
        return AzureLibNetwork.BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID;
    }

    public static BlockEntityAnimTriggerPacket receive(FriendlyByteBuf buf) {
        return new BlockEntityAnimTriggerPacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf());
    }

    @Override
    public void handle() {
        BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity instanceof GeoBlockEntity getBlockEntity) {
            getBlockEntity.triggerAnim(controllerName.isEmpty() ? null : controllerName, animName);
        }
    }
}
