package mod.azure.azurelib.cache;

import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Storage class that keeps track of the last animatable id used, and provides new ones on request.<br>
 * Generally only used for {@link net.minecraft.world.item.Item Items}, but any
 * {@link SingletonAnimatableInstanceCache singleton} will likely use this.
 */
public final class AnimatableIdCache extends SavedData {
	private static final String DATA_KEY = "AzureLib_id_cache";
	private long lastId;

	private AnimatableIdCache() {
		this(new CompoundTag());
	}

	private AnimatableIdCache(CompoundTag tag) {
		this.lastId = tag.getLong("last_id");
	}

    public static SavedData.Factory<AnimatableIdCache> factory() {
        return new SavedData.Factory<AnimatableIdCache>(AnimatableIdCache::new, AnimatableIdCache::new, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

    public static SavedData.Factory<AnimatableIdCache> factory2() {
        return new SavedData.Factory<AnimatableIdCache>(AnimatableIdCache::new, AnimatableIdCache::fromLegacy, DataFixTypes.SAVED_DATA_MAP_DATA);
    }

	/**
	 * Get the next free id from the id cache
	 * @param level An arbitrary ServerLevel. It doesn't matter which one
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
		var storage = level.getServer().overworld().getDataStorage();
		var cache = storage.computeIfAbsent(AnimatableIdCache.factory(), DATA_KEY);

		if (cache.lastId == 0) {
			AnimatableIdCache legacyCache = storage.get(AnimatableIdCache.factory2(), "AzureLib_ids");

			if (legacyCache != null)
				cache.lastId = legacyCache.lastId;
		}

		return cache;
	}

	/**
	 * Legacy wrapper for existing worlds pre-4.0.<br>
	 * Remove this at some point in the future
	 */
	private static AnimatableIdCache fromLegacy(CompoundTag tag) {
		var legacyCache = new AnimatableIdCache();

		for (var key : tag.getAllKeys()) 
			if (tag.contains(key, Tag.TAG_ANY_NUMERIC))
				legacyCache.lastId = Math.max(legacyCache.lastId, tag.getInt(key));

		return legacyCache;
	}
}
