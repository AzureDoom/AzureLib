/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.internal.common.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.core.animatable.GeoAnimatable;

/**
 * Packet for syncing user-definable animation data for {@link SingletonGeoAnimatable} instances
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public record AnimDataSyncPacket<D>(
    String syncableId,
    long instanceId,
    SerializableDataTicket<D> dataTicket,
    D data
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<AnimDataSyncPacket<?>> TYPE = new Type<>(
        AzureLibNetwork.ANIM_DATA_SYNC_PACKET_ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, AnimDataSyncPacket<?>> CODEC = StreamCodec.of(
        (buf, packet) -> {
            SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
            buf.writeUtf(packet.syncableId);
            buf.writeVarLong(packet.instanceId);
            ((StreamCodec) packet.dataTicket.streamCodec()).encode(buf, packet.data);
        },
        buf -> {
            final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

            return new AnimDataSyncPacket<>(
                buf.readUtf(),
                buf.readVarLong(),
                dataTicket,
                dataTicket.streamCodec().decode(buf)
            );
        }
    );

    @Override
    public void handle() {
        GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(syncableId);

        if (animatable instanceof SingletonGeoAnimatable singleton) {
            singleton.setAnimData(ClientUtils.getClientPlayer(), instanceId, dataTicket, data);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
