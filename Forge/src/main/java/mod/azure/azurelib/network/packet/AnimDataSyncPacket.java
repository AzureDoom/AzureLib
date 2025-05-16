/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.util.ClientUtils;

/**
 * Packet for syncing user-definable animation data for {@link SingletonGeoAnimatable} instances
 */
@Deprecated()
public class AnimDataSyncPacket<D> {

    private final String syncableId;

    private final long instanceId;

    private final SerializableDataTicket<D> dataTicket;

    private final D data;

    public AnimDataSyncPacket(String syncableId, long instanceId, SerializableDataTicket<D> dataTicket, D data) {
        this.syncableId = syncableId;
        this.instanceId = instanceId;
        this.dataTicket = dataTicket;
        this.data = data;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.syncableId);
        buffer.writeVarLong(this.instanceId);
        buffer.writeUtf(this.dataTicket.id());
        this.dataTicket.encode(this.data, buffer);
    }

    public static <D> AnimDataSyncPacket<D> decode(FriendlyByteBuf buffer) {
        String syncableId = buffer.readUtf();
        long instanceId = buffer.readVarLong();
        SerializableDataTicket<D> dataTicket = (SerializableDataTicket<D>) DataTickets.byName(buffer.readUtf());
        D data = dataTicket.decode(buffer);

        return new AnimDataSyncPacket<>(syncableId, instanceId, dataTicket, data);
    }

    public void receivePacket(Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context handler = context.get();

        handler.enqueueWork(() -> {
            GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(this.syncableId);

            if (animatable instanceof SingletonGeoAnimatable singleton)
                singleton.setAnimData(ClientUtils.getClientPlayer(), this.instanceId, this.dataTicket, this.data);
        });

        handler.setPacketHandled(true);
    }
}
