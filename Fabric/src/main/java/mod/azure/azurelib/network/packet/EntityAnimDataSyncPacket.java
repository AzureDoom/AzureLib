package mod.azure.azurelib.network.packet;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.constant.DataTickets;
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
import net.minecraft.world.entity.Entity;

/**
 * Packet for syncing user-definable animation data for
 * {@link net.minecraft.world.entity.Entity Entities}
 */
public class EntityAnimDataSyncPacket<D> extends AbstractPacket {
	private final int ENTITY_ID;
	private final SerializableDataTicket<D> DATA_TICKET;
	private final D DATA;

	public EntityAnimDataSyncPacket(int entityId, SerializableDataTicket<D> dataTicket, D data) {
		this.ENTITY_ID = entityId;
		this.DATA_TICKET = dataTicket;
		this.DATA = data;
	}

	@Override
	public FriendlyByteBuf encode() {
		FriendlyByteBuf buf = PacketByteBufs.create();

		buf.writeVarInt(this.ENTITY_ID);
		buf.writeUtf(this.DATA_TICKET.id());
		this.DATA_TICKET.encode(this.DATA, buf);

		return buf;
	}

	@Override
	public ResourceLocation getPacketID() {
		return AzureLibNetwork.ENTITY_ANIM_DATA_SYNC_PACKET_ID;
	}

	public static <D> void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
		final int ENTITY_ID = buf.readVarInt();
		final SerializableDataTicket<D> DATA_TICKET = (SerializableDataTicket<D>) DataTickets.byName(buf.readUtf());
		final D DATA = DATA_TICKET.decode(buf);

		client.execute(() -> runOnThread(ENTITY_ID, DATA_TICKET, DATA));
	}

	private static <D> void runOnThread(int entityId, SerializableDataTicket<D> dataTicket, D data) {
		Entity entity = ClientUtils.getLevel().getEntity(entityId);

		if (entity instanceof GeoEntity geoEntity)
			geoEntity.setAnimData(dataTicket, data);
	}
}
