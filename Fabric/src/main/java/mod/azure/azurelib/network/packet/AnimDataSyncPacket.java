package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.animatable.SingletonGeoAnimatable;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.network.AbstractPacket;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.util.ClientUtils;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Packet for syncing user-definable animation data for
 * {@link SingletonGeoAnimatable} instances
 */
public class AnimDataSyncPacket<D> extends AbstractPacket {
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

	@Override
    public FriendlyByteBuf encode() {
        FriendlyByteBuf buf = PacketByteBufs.create();

        buf.writeUtf(this.syncableId);
        buf.writeVarLong(this.instanceId);
        buf.writeUtf(this.dataTicket.id());
        this.dataTicket.encode(this.data, buf);

        return buf;
    }

	@Override
	public ResourceLocation getPacketID() {
		return AzureLibNetwork.ANIM_DATA_SYNC_PACKET_ID;
	}

	public static <D> void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		String syncableId = buf.readUtf();
		long instanceID = buf.readVarLong();
		SerializableDataTicket<D> dataTicket = (SerializableDataTicket<D>)DataTickets.byName(buf.readUtf());
		D data = dataTicket.decode(buf);

        client.execute(() -> runOnThread(syncableId, instanceID, dataTicket, data));
    }

	private static <D> void runOnThread(String syncableId, long instanceId, SerializableDataTicket<D> dataTicket, D data) {
		GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(syncableId);

		if (animatable instanceof SingletonGeoAnimatable)
			((SingletonGeoAnimatable) animatable).setAnimData(ClientUtils.getClientPlayer(), instanceId, dataTicket, data);
	}
}
