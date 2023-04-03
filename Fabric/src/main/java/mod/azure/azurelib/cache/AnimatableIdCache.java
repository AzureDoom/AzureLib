package mod.azure.azurelib.cache;

import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Storage class that keeps track of the last animatable id used, and provides new ones on request.<br>
 * Generally only used for {@link net.minecraft.world.item.Item Items}, but any {@link SingletonAnimatableInstanceCache singleton} will likely use this.
 */
public final class AnimatableIdCache extends SavedData {
	private static final String DATA_KEY = "AzureLib_id_cache";
	private long lastId;

	private AnimatableIdCache() {
		super(DATA_KEY);
	}

	/**
	 * Get the next free id from the id cache
	 * 
	 * @param level An arbitrary ServerWorld. It doesn't matter which one
	 * @return The next free ID, which is immediately reserved for use after calling this method
	 */
	public static long getFreeId(ServerLevel level) {
		return getCache(level).getNextId();
	}

	private long getNextId() {
		setDirty();

		return ++this.lastId;
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		tag.putLong("last_id", this.lastId);

		return tag;
	}

	private static AnimatableIdCache getCache(ServerLevel level) {
		DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
		AnimatableIdCache cache = storage.computeIfAbsent(AnimatableIdCache::new, DATA_KEY);

		return cache;
	}

	/**
	 * Legacy wrapper for existing worlds pre-4.0.<br>
	 * Remove this at some point in the future
	 */
	public void load(CompoundTag tag) {
		AnimatableIdCache legacyCache = new AnimatableIdCache();
		for (String key : tag.getAllKeys()) {
			if (tag.contains(key, 99))
				legacyCache.lastId = Math.max(legacyCache.lastId, tag.getInt(key));
		}
	}
}
