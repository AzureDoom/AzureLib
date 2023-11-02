package mod.azure.azurelib.common.api.common.animatable;

import mod.azure.azurelib.common.api.client.renderer.GeoReplacedEntityRenderer;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.internal.common.network.packet.EntityAnimDataSyncPacket;
import mod.azure.azurelib.common.internal.common.network.packet.EntityAnimTriggerPacket;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import net.minecraft.world.entity.Entity;

/**
 * The {@link GeoAnimatable} interface specific to {@link net.minecraft.world.entity.Entity Entities}. This also applies to Projectiles and other Entity subclasses.<br>
 * <b>NOTE:</b> This <u>cannot</u> be used for entities using the {@link GeoReplacedEntityRenderer} as you aren't extending {@code Entity}. Use {@link GeoReplacedEntity} instead.
 */
public interface GeoEntity extends GeoAnimatable {
	/**
	 * Get server-synced animation data via its relevant {@link SerializableDataTicket}.<br>
	 * Should only be used on the <u>client-side</u>.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param dataTicket The data ticket for the data to retrieve
	 * @return The synced data, or null if no data of that type has been synced
	 */
	@Nullable
	default <D> D getAnimData(SerializableDataTicket<D> dataTicket) {
		return getAnimatableInstanceCache().getManagerForId(((Entity) this).getId()).getData(dataTicket);
	}

	/**
	 * Saves an arbitrary syncable piece of data to this animatable's {@link AnimatableManager}.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param dataTicket The DataTicket to sync the data for
	 * @param data       The data to sync
	 */
	default <D> void setAnimData(SerializableDataTicket<D> dataTicket, D data) {
		Entity entity = (Entity) this;

		if (entity.level().isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(entity.getId()).setData(dataTicket, data);
		} else {
			EntityAnimDataSyncPacket<D> entityAnimDataSyncPacket = new EntityAnimDataSyncPacket<>(entity.getId(), dataTicket, data);
			Services.NETWORK.sendToTrackingEntityAndSelf(entityAnimDataSyncPacket, entity);
		}
	}

	/**
	 * Trigger an animation for this Entity, based on the controller name and animation name.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * 
	 * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
	 * @param animName       The name of animation to trigger. This needs to have been registered with the controller via {@link AnimationController#triggerableAnim AnimationController.triggerableAnim}
	 */
	default void triggerAnim(@Nullable String controllerName, String animName) {
		Entity entity = (Entity) this;

		if (entity.level().isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(entity.getId()).tryTriggerAnimation(controllerName, animName);
		} else {
			EntityAnimTriggerPacket entityAnimTriggerPacket = new EntityAnimTriggerPacket(entity.getId(), controllerName, animName);
			Services.NETWORK.sendToTrackingEntityAndSelf(entityAnimTriggerPacket, entity);
		}
	}

	/**
	 * Returns the current age/tick of the animatable instance.<br>
	 * By default this is just the animatable's age in ticks, but this method allows for non-ticking custom animatables to provide their own values
	 * 
	 * @param entity The Entity representing this animatable
	 * @return The current tick/age of the animatable, for animation purposes
	 */
	@Override
	default double getTick(Object entity) {
		return RenderUtils.getCurrentTick();
	}
}
