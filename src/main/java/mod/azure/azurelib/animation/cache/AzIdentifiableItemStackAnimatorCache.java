package mod.azure.azurelib.animation.cache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.util.AzItemIds;

/**
 * The AzIdentifiableItemStackAnimatorCache class is a singleton utility for managing a cache of {@link ItemStack}
 * objects, each associated with a unique identifier (UUID). This class provides functionality to register and retrieve
 * item animators that apply to specific {@link ItemStack}s using their respective UUIDs.
 */
public class AzIdentifiableItemStackAnimatorCache {

    private static final AzIdentifiableItemStackAnimatorCache INSTANCE = new AzIdentifiableItemStackAnimatorCache();

    // TODO: Purge animators periodically.
    private static final Map<UUID, AzItemAnimator> ANIMATORS_BY_UUID = new HashMap<>();

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    public void add(ItemStack itemStack, AzItemAnimator animator) {
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag != null && AzItemIds.has(tag)) {
            ANIMATORS_BY_UUID.computeIfAbsent(AzItemIds.get(tag), ($) -> animator);
        }
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid) {
        return uuid == null ? null : ANIMATORS_BY_UUID.get(uuid);
    }
}
