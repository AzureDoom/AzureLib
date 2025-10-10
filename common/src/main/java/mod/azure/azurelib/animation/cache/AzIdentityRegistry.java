package mod.azure.azurelib.animation.cache;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import mod.azure.azurelib.AzureLib;

/**
 * The AzIdentityRegistry class provides functionality to register and check the identity of items. This class maintains
 * a static registry of unique items, allowing you to determine if a specific item has been registered.
 */
public class AzIdentityRegistry {

    private static final Set<Item> IDENTITY_OF_ITEMS = new HashSet<>();

    /**
     * Registers one or more items into a static identity set, ensuring that the items are stored for identity tracking.
     *
     * @param first The first non-null item to be registered. This parameter is mandatory.
     * @param rest  A var-args array of additional items to register. These items can be null, but null values will not
     *              be added to the identity set.
     */
    public static void register(@NotNull Item first, Item... rest) {
        IDENTITY_OF_ITEMS.add(first);
        IDENTITY_OF_ITEMS.addAll(Arrays.asList(rest));

        first.getDefaultInstance().getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        for (Item item : rest) {
            item.getDefaultInstance().getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
    }

    /**
     * Checks if the specified item exists in the identity set.
     *
     * @param item The item to check for identity. Must not be null.
     * @return true if the item exists in the identity set, false otherwise.
     */
    public static boolean hasIdentity(Item item) {
        return IDENTITY_OF_ITEMS.contains(item);
    }
}
