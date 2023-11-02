package mod.azure.azurelib.common.api.common.animatable;

import mod.azure.azurelib.common.internal.common.AzureLib;
import mod.azure.azurelib.common.internal.common.core.animation.AnimationController;
import mod.azure.azurelib.common.platform.Services;
import org.jetbrains.annotations.Nullable;

import mod.azure.azurelib.common.internal.common.core.animatable.GeoAnimatable;
import mod.azure.azurelib.common.internal.common.core.animation.AnimatableManager;
import mod.azure.azurelib.common.internal.common.network.SerializableDataTicket;
import mod.azure.azurelib.common.internal.common.network.packet.BlockEntityAnimDataSyncPacket;
import mod.azure.azurelib.common.internal.common.network.packet.BlockEntityAnimTriggerPacket;
import mod.azure.azurelib.common.internal.client.util.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * The {@link GeoAnimatable} interface specific to {@link BlockEntity BlockEntities}
 */
public interface GeoBlockEntity extends GeoAnimatable {
	/**
	 * Get server-synced animation data via its relevant {@link SerializableDataTicket}.<br>
	 * Should only be used on the <u>client-side</u>.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * @param dataTicket The data ticket for the data to retrieve
	 * @return The synced data, or null if no data of that type has been synced
	 */
	@Nullable
	default <D> D getAnimData(SerializableDataTicket<D> dataTicket) {
		return getAnimatableInstanceCache().getManagerForId(0).getData(dataTicket);
	}

	/**
	 * Saves an arbitrary piece of data to this animatable's {@link AnimatableManager}.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * @param dataTicket The DataTicket to sync the data for
	 * @param data The data to sync
	 */
	default <D> void setAnimData(SerializableDataTicket<D> dataTicket, D data) {
		BlockEntity blockEntity = (BlockEntity)this;
		Level level = blockEntity.getLevel();

		if (level == null) {
			AzureLib.LOGGER.error("Attempting to set animation data for BlockEntity too early! Must wait until after the BlockEntity has been set in the world. ({})", blockEntity.getClass());

			return;
		}

		if (level.isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(0).setData(dataTicket, data);
		}
		else {
			BlockPos pos = blockEntity.getBlockPos();

			BlockEntityAnimDataSyncPacket<D> blockEntityAnimDataSyncPacket = new BlockEntityAnimDataSyncPacket<>(pos, dataTicket, data);
			Services.NETWORK.sendToEntitiesTrackingChunk(blockEntityAnimDataSyncPacket, (ServerLevel) level, pos);
		}
	}

	/**
	 * Trigger an animation for this BlockEntity, based on the controller name and animation name.<br>
	 * <b><u>DO NOT OVERRIDE</u></b>
	 * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
	 * @param animName The name of animation to trigger. This needs to have been registered with the controller via {@link AnimationController#triggerableAnim AnimationController.triggerableAnim}
	 */
	default void triggerAnim(@Nullable String controllerName, String animName) {
		BlockEntity blockEntity = (BlockEntity)this;
		Level level = blockEntity.getLevel();

		if (level == null) {
			AzureLib.LOGGER.error("Attempting to trigger an animation for a BlockEntity too early! Must wait until after the BlockEntity has been set in the world. ({})", blockEntity.getClass());

			return;
		}

		if (level.isClientSide()) {
			getAnimatableInstanceCache().getManagerForId(0).tryTriggerAnimation(controllerName, animName);
		}
		else {
			BlockPos pos = blockEntity.getBlockPos();

			BlockEntityAnimTriggerPacket blockEntityAnimTriggerPacket = new BlockEntityAnimTriggerPacket(pos, controllerName, animName);
			Services.NETWORK.sendToEntitiesTrackingChunk(blockEntityAnimTriggerPacket, (ServerLevel) level, pos);
		}
	}

	/**
	 * Returns the current age/tick of the animatable instance.<br>
	 * By default this is just the animatable's age in ticks, but this method allows for non-ticking custom animatables to provide their own values
	 * @param blockEntity The BlockEntity representing this animatable
	 * @return The current tick/age of the animatable, for animation purposes
	 */
	@Override
	default double getTick(Object blockEntity) {
		return RenderUtils.getCurrentTick();
	}
}
