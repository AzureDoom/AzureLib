package mod.azure.azurelib.animatable;

import javax.annotation.Nullable;

import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.network.AzureLibNetwork;
import mod.azure.azurelib.network.SerializableDataTicket;
import mod.azure.azurelib.network.packet.AnimDataSyncPacket;
import mod.azure.azurelib.network.packet.AnimTriggerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

/**
 * The {@link GeoAnimatable} interface specific to singleton objects. This primarily applies to armor and items
 * 
 * @see <a href="https://github.com/bernie-g/AzureLib/wiki/Item-Animations">AzureLib Wiki - Item Animations</a>
 */
public interface SingletonGeoAnimatable extends GeoAnimatable {
	/**
	 * Register this as a synched {@code GeoAnimatable} instance with AzureLib's networking functions.<br>
	 * This should be called inside the constructor of your object.
	 */
	static void registerSyncedAnimatable(GeoAnimatable animatable) {
		AzureLibNetwork.registerSyncedAnimatable(animatable);
	}

	/**
	 * Get server-synced animation data via its relevant {@link SerializableDataTicket}.<br>
	 * Should only be used on the <u>client-side</u>.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param instanceId The animatable's instance id
	 * @param dataTicket The data ticket for the data to retrieve
	 * @return The synced data, or null if no data of that type has been synced
	 */
	@Nullable
	default <D> D getAnimData(long instanceId, SerializableDataTicket<D> dataTicket) {
		return getAnimatableInstanceCache().getManagerForId(instanceId).getData(dataTicket);
	}

	/**
	 * Saves an arbitrary piece of syncable data to this animatable's {@link AnimatableManager}.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param relatedEntity An entity related to the state of the data for syncing (E.G. The player holding the item)
	 * @param instanceId    The unique id that identifies the specific animatable instance
	 * @param dataTicket    The DataTicket to sync the data for
	 * @param data          The data to sync
	 */
	default <D> void setAnimData(Entity relatedEntity, long instanceId, SerializableDataTicket<D> dataTicket, D data) {
		if (relatedEntity.level.isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(instanceId).setData(dataTicket, data);
		} else {
			syncAnimData(instanceId, dataTicket, data, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> relatedEntity));
		}
	}

	/**
	 * Syncs an arbitrary piece of data to all players targeted by the packetTarget.<br>
	 * This method should only be called on the <u>server side</u>.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 *
	 * @param instanceId   The unique id that identifies the specific animatable instance
	 * @param dataTicket   The DataTicket to sync the data for
	 * @param data         The data to sync
	 * @param packetTarget The distribution method determining which players to sync the data to
	 */
	default <D> void syncAnimData(long instanceId, SerializableDataTicket<D> dataTicket, D data, PacketDistributor.PacketTarget packetTarget) {
		AzureLibNetwork.send(new AnimDataSyncPacket<>(getClass().toString(), instanceId, dataTicket, data), packetTarget);
	}

	/**
	 * Trigger a client-side animation for this GeoAnimatable for the given controller name and animation name.<br>
	 * This can be fired from either the client or the server, but optimally you would call it from the server.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param relatedEntity  An entity related to the animatable to trigger the animation for (E.G. The player holding the item)
	 * @param instanceId     The unique id that identifies the specific animatable instance
	 * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
	 * @param animName       The name of animation to trigger. This needs to have been registered with the controller via {@link mod.azure.azurelib.core.animation.AnimationController#triggerableAnim AnimationController.triggerableAnim}
	 */
	default <D> void triggerAnim(Entity relatedEntity, long instanceId, @Nullable String controllerName, String animName) {
		if (relatedEntity.level.isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(instanceId).tryTriggerAnimation(controllerName, animName);
		} else {
			AzureLibNetwork.send(new AnimTriggerPacket<>(getClass().toString(), instanceId, controllerName, animName), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> relatedEntity));
		}
	}

	/**
	 * Remotely triggers a client-side animation for this GeoAnimatable for all players targeted by the packetTarget.<br>
	 * This method should only be called on the <u>server side</u>.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 *
	 * @param instanceId     The unique id that identifies the specific animatable instance
	 * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
	 * @param animName       The name of animation to trigger. This needs to have been registered with the controller via {@link mod.azure.azurelib.core.animation.AnimationController#triggerableAnim AnimationController.triggerableAnim}
	 * @param packetTarget   The distribution method determining which players to sync the data to
	 */
	default <D> void triggerAnim(long instanceId, @Nullable String controllerName, String animName, PacketDistributor.PacketTarget packetTarget) {
		AzureLibNetwork.send(new AnimTriggerPacket<>(getClass().toString(), instanceId, controllerName, animName), packetTarget);
	}

	/**
	 * Override the default handling for instantiating an AnimatableInstanceCache for this animatable.<br>
	 * Don't override this unless you know what you're doing.
	 */
	@Override
	default @Nullable AnimatableInstanceCache animatableCacheOverride() {
		return new SingletonAnimatableInstanceCache(this);
	}
}
