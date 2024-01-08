package mod.azure.azurelib.common.internal.common.network.packet;

import mod.azure.azurelib.common.api.common.animatable.GeoEntity;
import mod.azure.azurelib.common.internal.common.constant.DataTickets;
import mod.azure.azurelib.common.internal.common.network.AbstractPacket;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.platform.services.AzureLibNetwork;
import mod.azure.azurelib.common.api.client.helper.ClientUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Packet for syncing user-definable animation data for
 * {@link net.minecraft.world.entity.Entity Entities}
 */
public class EntityAnimDataSyncPacket<D> extends AbstractPacket {
	private final int entityId;
	private final SerializableDataTicket<D> dataTicket;
	private final D data;

	public EntityAnimDataSyncPacket(int entityId, SerializableDataTicket<D> dataTicket, D data) {
		this.entityId = entityId;
		this.dataTicket = dataTicket;
		this.data = data;
	}

	@Override
	public void write(FriendlyByteBuf buf) {
		buf.writeVarInt(this.entityId);
		buf.writeUtf(this.dataTicket.id());
		this.dataTicket.encode(this.data, buf);
	}

	@Override
	public ResourceLocation id() {
		return AzureLibNetwork.ENTITY_ANIM_DATA_SYNC_PACKET_ID;
	}

	public static <D> EntityAnimDataSyncPacket<D> receive(FriendlyByteBuf buf) {
		int entityId = buf.readVarInt();
		SerializableDataTicket<D> dataTicket = (SerializableDataTicket<D>) DataTickets.byName(buf.readUtf());

		return new EntityAnimDataSyncPacket<>(entityId, dataTicket, dataTicket.decode(buf));
	}

	@Override
	public void handle() {
		Entity entity = ClientUtils.getLevel().getEntity(entityId);

		if (entity instanceof GeoEntity geoEntity) {
			geoEntity.setAnimData(dataTicket, data);
		}
	}
}
