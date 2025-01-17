/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoBlockEntity;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for
 * {@link net.minecraft.world.level.block.entity.BlockEntity BlockEntities}
 *
 * @deprecated
 */
public record BlockEntityAnimTriggerPacket(
    BlockPos blockPos,
    @Nullable String controllerName,
    String animName
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<BlockEntityAnimTriggerPacket> TYPE = new Type<>(
        AzureLibNetwork.BLOCK_ENTITY_ANIM_TRIGGER_SYNC_PACKET_ID
    );

    public static final StreamCodec<FriendlyByteBuf, BlockEntityAnimTriggerPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        BlockEntityAnimTriggerPacket::blockPos,
        ByteBufCodecs.STRING_UTF8,
        BlockEntityAnimTriggerPacket::controllerName,
        ByteBufCodecs.STRING_UTF8,
        BlockEntityAnimTriggerPacket::animName,
        BlockEntityAnimTriggerPacket::new
    );

    @Override
    public void handle() {
        BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity instanceof GeoBlockEntity getBlockEntity) {
            getBlockEntity.triggerAnim(controllerName.isEmpty() ? null : controllerName, animName);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
