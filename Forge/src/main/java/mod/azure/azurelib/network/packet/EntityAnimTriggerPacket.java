/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.network.packet;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.animatable.GeoReplacedEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.util.ClientUtils;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Packet for syncing user-definable animations that can be triggered from the server for {@link Entity Entities}
 */
@Deprecated()
public class EntityAnimTriggerPacket<D> {
	private final int entityId;
	private final boolean isReplacedEntity;
	private final String controllerName;
	private final String animName;

	public EntityAnimTriggerPacket(int entityId, @Nullable String controllerName, String animName) {
		this(entityId, false, controllerName, animName);
	}

	public EntityAnimTriggerPacket(int entityId, boolean isReplacedEntity, @Nullable String controllerName, String animName) {
		this.entityId = entityId;
		this.isReplacedEntity = isReplacedEntity;
		this.controllerName = controllerName == null ? "" : controllerName;
		this.animName = animName;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(this.entityId);
		buffer.writeBoolean(this.isReplacedEntity);
		buffer.writeString(this.controllerName);
		buffer.writeString(this.animName);
	}

	public static <D> EntityAnimTriggerPacket<D> decode(PacketBuffer buffer) {
		return new EntityAnimTriggerPacket<>(buffer.readVarInt(), buffer.readBoolean(), buffer.readString(), buffer.readString());
	}

	public void receivePacket(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context handler = context.get();

		handler.enqueueWork(() -> {
			Entity entity = ClientUtils.getLevel().getEntityByID(this.entityId);

			if (entity == null)
				return;

			if (this.isReplacedEntity) {
				GeoAnimatable animatable = RenderUtils.getReplacedAnimatable(entity.getType());

				if (animatable instanceof GeoReplacedEntity)
					((GeoReplacedEntity) animatable).triggerAnim(entity, this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
			} else if (entity instanceof GeoEntity) {
				((GeoEntity) entity).triggerAnim(this.controllerName.isEmpty() ? null : this.controllerName, this.animName);
			}
		});
		handler.setPacketHandled(true);
	}
}
