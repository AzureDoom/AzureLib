/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoBlockEntity;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

/**
 * Packet for syncing user-definable animation data for {@link BlockEntity BlockEntities}
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public record BlockEntityAnimDataSyncPacket<D>(
    BlockPos blockPos,
    SerializableDataTicket<D> dataTicket,
    D data
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<BlockEntityAnimDataSyncPacket<?>> TYPE = new Type<>(
        AzureLibNetwork.BLOCK_ENTITY_ANIM_DATA_SYNC_PACKET_ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityAnimDataSyncPacket<?>> CODEC = StreamCodec.of(
        (buf, packet) -> {
            SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
            buf.writeBlockPos(packet.blockPos);
            ((StreamCodec) packet.dataTicket.streamCodec()).encode(buf, packet.data);
        },
        buf -> {
            final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

            return new BlockEntityAnimDataSyncPacket<>(
                buf.readBlockPos(),
                dataTicket,
                dataTicket.streamCodec().decode(buf)
            );
        }
    );

    @Override
    public void handle() {
        BlockEntity blockEntity = ClientUtils.getLevel().getBlockEntity(blockPos);

        if (blockEntity instanceof GeoBlockEntity geoBlockEntity) {
            geoBlockEntity.setAnimData(dataTicket, data);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
