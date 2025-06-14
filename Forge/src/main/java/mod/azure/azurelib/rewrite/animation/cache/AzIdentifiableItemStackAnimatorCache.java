package mod.azure.azurelib.rewrite.animation.cache;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;

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
        if (!itemStack.hasTag()) {
            itemStack.setTag(new CompoundNBT());
        }

        CompoundNBT tag = itemStack.getOrCreateTag();
        UUID uuid = tag.getUniqueId(AzureLib.ITEM_UUID_TAG);

        if (uuid != null) {
            ANIMATORS_BY_UUID.computeIfAbsent(uuid, ($) -> animator);
        }
    }

    public AzItemAnimator getOrNull(UUID uuid) {
        return uuid == null ? null : ANIMATORS_BY_UUID.get(uuid);
    }
}
