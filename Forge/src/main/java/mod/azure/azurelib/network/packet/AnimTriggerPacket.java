package mod.azure.azurelib.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.network.AzureLibNetwork;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * Packet for syncing user-definable animations that can be triggered from the server
 */
public class AnimTriggerPacket<D> {
	private final String syncableId;
	private final long instanceId;
	private final String controllerName;
	private final String animName;

	public AnimTriggerPacket(String syncableId, long instanceId, @Nullable String controllerName, String animName) {
		this.syncableId = syncableId;
		this.instanceId = instanceId;
		this.controllerName = controllerName == null ? "" : controllerName;
		this.animName = animName;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeUtf(this.syncableId);
		buffer.writeVarLong(this.instanceId);
		buffer.writeUtf(this.controllerName);
		buffer.writeUtf(this.animName);
	}

	public static <D> AnimTriggerPacket<D> decode(FriendlyByteBuf buffer) {
		return new AnimTriggerPacket<>(buffer.readUtf(), buffer.readVarLong(), buffer.readUtf(), buffer.readUtf());
	}

	public void receivePacket(Supplier<NetworkEvent.Context> context) {
		NetworkEvent.Context handler = context.get();

		handler.enqueueWork(() -> {
			GeoAnimatable animatable = AzureLibNetwork.getSyncedAnimatable(this.syncableId);

			if (animatable != null) {
				AnimatableManager<?> manager = animatable.getAnimatableInstanceCache().getManagerForId(this.instanceId);

				manager.tryTriggerAnimation(this.controllerName, this.animName);
			}
		});
		handler.setPacketHandled(true);
	}
}
