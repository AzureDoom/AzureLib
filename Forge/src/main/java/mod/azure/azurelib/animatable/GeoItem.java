package mod.azure.azurelib.animatable;

import java.util.EnumMap;
import java.util.Map;

import mod.azure.azurelib.cache.AnimatableIdCache;
import mod.azure.azurelib.constant.DataTickets;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.ContextAwareAnimatableManager;
import mod.azure.azurelib.util.RenderUtils;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;

/**
 * The {@link mod.azure.azurelib.core.animatable.GeoAnimatable GeoAnimatable} interface specific to {@link net.minecraft.world.item.Item Items}. This also applies to armor, as they are just items too.
 */
public interface GeoItem extends SingletonGeoAnimatable {
	static final String ID_NBT_KEY = "AzureLibID";

	/**
	 * Gets the unique identifying number from this ItemStack's {@link net.minecraft.nbt.Tag NBT}, or {@link Long#MAX_VALUE} if one hasn't been assigned
	 */
	static long getId(ItemStack stack) {
		CompoundTag tag = stack.getTag();

		if (tag == null)
			return Long.MAX_VALUE;

		return tag.getLong(ID_NBT_KEY);
	}

	/**
	 * Gets the unique identifying number from this ItemStack's {@link net.minecraft.nbt.Tag NBT}.<br>
	 * If no ID has been reserved for this stack yet, it will reserve a new id and assign it
	 */
	static long getOrAssignId(ItemStack stack, ServerLevel level) {
		CompoundTag tag = stack.getOrCreateTag();
		long id = tag.getLong(ID_NBT_KEY);

		if (tag.contains(ID_NBT_KEY, Tag.TAG_ANY_NUMERIC))
			return id;

		id = AnimatableIdCache.getFreeId(level);

		tag.putLong(ID_NBT_KEY, id);

		return id;
	}

	/**
	 * Returns the current age/tick of the animatable instance.<br>
	 * By default this is just the animatable's age in ticks, but this method allows for non-ticking custom animatables to provide their own values
	 * 
	 * @param itemStack The ItemStack representing this animatable
	 * @return The current tick/age of the animatable, for animation purposes
	 */
	@Override
	default double getTick(Object itemStack) {
		return RenderUtils.getCurrentTick();
	}

	/**
	 * Whether this item animatable is perspective aware, handling animations differently depending on the {@link net.minecraft.world.item.ItemDisplayContext render perspective}
	 */
	default boolean isPerspectiveAware() {
		return false;
	}

	/**
	 * Replaces the default AnimatableInstanceCache for GeoItems if {@link GeoItem#isPerspectiveAware()} is true, for perspective-dependent handling
	 */
	@Override
	default AnimatableInstanceCache animatableCacheOverride() {
		if (isPerspectiveAware())
			return new ContextBasedAnimatableInstanceCache(this);

		return SingletonGeoAnimatable.super.animatableCacheOverride();
	}

	/**
	 * AnimatableInstanceCache specific to GeoItems, for doing render perspective based animations
	 */
	class ContextBasedAnimatableInstanceCache extends SingletonAnimatableInstanceCache {
		public ContextBasedAnimatableInstanceCache(GeoAnimatable animatable) {
			super(animatable);
		}

		/**
		 * Gets an {@link AnimatableManager} instance from this cache, cached under the id provided, or a new one if one doesn't already exist.<br>
		 * This subclass assumes that all animatable instances will be sharing this cache instance, and so differentiates data by ids.
		 */
		@Override
		public AnimatableManager<?> getManagerForId(long uniqueId) {
			if (!this.managers.containsKey(uniqueId))
				this.managers.put(uniqueId, new ContextAwareAnimatableManager<GeoItem, TransformType>(this.animatable) {
					@Override
					protected Map<TransformType, AnimatableManager<GeoItem>> buildContextOptions(GeoAnimatable animatable) {
						Map<TransformType, AnimatableManager<GeoItem>> map = new EnumMap<>(TransformType.class);

						for (TransformType context : TransformType.values()) {
							map.put(context, new AnimatableManager<>(animatable));
						}

						return map;
					}

					@Override
					public TransformType getCurrentContext() {
						TransformType context = getData(DataTickets.ITEM_RENDER_PERSPECTIVE);

						return context == null ? TransformType.NONE : context;
					}
				});

			return this.managers.get(uniqueId);
		}
	}
}
