/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.network.packet;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.util.ClientUtils;

/**
 * Packet for syncing user-definable animation data for {@link Entity Entities}
 */
@Deprecated()
public class EntityAnimDataSyncPacket<D> {

    private final int entityId;

    private final SerializableDataTicket<D> dataTicket;

    private final D data;

    public EntityAnimDataSyncPacket(int entityId, SerializableDataTicket<D> dataTicket, D data) {
        this.entityId = entityId;
        this.dataTicket = dataTicket;
        this.data = data;
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeVarInt(this.entityId);
        buffer.writeUtf(this.dataTicket.id());
        this.dataTicket.encode(this.data, buffer);
    }

    public static <D> EntityAnimDataSyncPacket<D> decode(PacketBuffer buffer) {
        int entityId = buffer.readVarInt();
        SerializableDataTicket<D> dataTicket = (SerializableDataTicket<D>) DataTickets.byName(buffer.readUtf());

        return new EntityAnimDataSyncPacket<>(entityId, dataTicket, dataTicket.decode(buffer));
    }

    public void receivePacket(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();

        handler.enqueueWork(() -> {
            Entity entity = ClientUtils.getLevel().getEntity(this.entityId);

            if (entity instanceof GeoEntity)
                ((GeoEntity) entity).setAnimData(this.dataTicket, this.data);
        });
        handler.setPacketHandled(true);
    }
}
