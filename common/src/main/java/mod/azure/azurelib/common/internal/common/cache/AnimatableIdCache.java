/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.common.cache;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import mod.azure.azurelib.core.animatable.instance.SingletonAnimatableInstanceCache;

/**
 * Storage class that keeps track of the last animatable id used, and provides new ones on request.<br>
 * Generally only used for {@link net.minecraft.world.item.Item Items}, but any {@link SingletonAnimatableInstanceCache
 * singleton} will likely use this.
 *
 * @deprecated
 */
@Deprecated(forRemoval = true)
public final class AnimatableIdCache extends SavedData {

    private static final String DATA_KEY = "AzureLib_id_cache";

    private long lastId;

    private AnimatableIdCache() {}

    private AnimatableIdCache(CompoundTag tag, HolderLookup.Provider registryLookup) {
        this.lastId = tag.getLong("last_id");
    }

    public static SavedData.Factory<AnimatableIdCache> factory() {
        return new SavedData.Factory<>(
            AnimatableIdCache::new,
            AnimatableIdCache::new,
            null
        );
    }

    /**
     * Get the next free id from the id cache
     *
     * @param level An arbitrary ServerLevel. It doesn't matter which one
     * @return The next free ID, which is immediately reserved for use after calling this method
     */
    public static long getFreeId(ServerLevel level) {
        return getCache(level).getNextId();
    }

    private static AnimatableIdCache getCache(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(AnimatableIdCache.factory(), DATA_KEY);
    }

    private long getNextId() {
        setDirty();
        return ++this.lastId;
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.@NotNull Provider var2) {
        tag.putLong("last_id", this.lastId);
        return tag;
    }
}
