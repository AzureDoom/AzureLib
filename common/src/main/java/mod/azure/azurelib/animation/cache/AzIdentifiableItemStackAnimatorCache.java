package mod.azure.azurelib.animation.cache;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.impl.AzItemAnimator;

/**
 * The AzIdentifiableItemStackAnimatorCache class is a singleton utility for managing a cache of {@link ItemStack}
 * objects, each associated with a unique identifier (UUID). This class provides functionality to register and retrieve
 * item animators that apply to specific {@link ItemStack}s using their respective UUIDs.
 */
public class AzIdentifiableItemStackAnimatorCache {

    private static final int MAX_CACHED_ANIMATORS = 256;

    private static final AzIdentifiableItemStackAnimatorCache INSTANCE = new AzIdentifiableItemStackAnimatorCache();

    private static final Map<UUID, AzItemAnimator> ANIMATORS_BY_UUID = Collections.synchronizedMap(
        new LinkedHashMap<>(16, 0.75F, true) {

            @Override
            protected boolean removeEldestEntry(Map.Entry<UUID, AzItemAnimator> eldest) {
                return size() > MAX_CACHED_ANIMATORS;
            }
        }
    );

    public static AzIdentifiableItemStackAnimatorCache getInstance() {
        return INSTANCE;
    }

    private AzIdentifiableItemStackAnimatorCache() {}

    public void add(ItemStack itemStack, AzItemAnimator animator) {
        var uuid = itemStack.get(AzureLib.AZ_ID.get());

        if (uuid != null) {
            ANIMATORS_BY_UUID.computeIfAbsent(uuid, ($) -> animator);
        }
    }

    public @Nullable AzItemAnimator getOrNull(UUID uuid) {
        return uuid == null ? null : ANIMATORS_BY_UUID.get(uuid);
    }
}
