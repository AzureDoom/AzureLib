/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.network.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.api.common.animatable.GeoReplacedEntity;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;

/**
 * Packet for syncing user-definable animation data for {@link net.minecraft.world.entity.Entity Entities}
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public record EntityAnimDataSyncPacket<D>(
    int entityId,
    boolean isReplacedEntity,
    SerializableDataTicket<D> dataTicket,
    D data
) implements AbstractPacket {

    public static final CustomPacketPayload.Type<EntityAnimDataSyncPacket<?>> TYPE = new Type<>(
        AzureLibNetwork.ENTITY_ANIM_DATA_SYNC_PACKET_ID
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, EntityAnimDataSyncPacket<?>> CODEC = StreamCodec.of(
        (buf, packet) -> {
            SerializableDataTicket.STREAM_CODEC.encode(buf, packet.dataTicket);
            buf.writeVarInt(packet.entityId);
            buf.writeBoolean(packet.isReplacedEntity);
            ((StreamCodec) packet.dataTicket.streamCodec()).encode(buf, packet.data);
        },
        buf -> {
            final SerializableDataTicket dataTicket = SerializableDataTicket.STREAM_CODEC.decode(buf);

            return new EntityAnimDataSyncPacket<>(
                buf.readVarInt(),
                buf.readBoolean(),
                dataTicket,
                dataTicket.streamCodec().decode(buf)
            );
        }
    );

    @Override
    public void handle() {
        Entity entity = ClientUtils.getLevel().getEntity(this.entityId);
        if (entity == null)
            return;
        if (!this.isReplacedEntity) {
            if (entity instanceof GeoEntity geoEntity)
                geoEntity.setAnimData(this.dataTicket, this.data);
            return;
        }
        if (RenderUtils.getReplacedAnimatable(entity.getType()) instanceof GeoReplacedEntity replacedEntity)
            replacedEntity.setAnimData(entity, this.dataTicket, this.data);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
