package mod.azure.azurelib.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;

/**
 * Reads and writes AzureLib's per-stack ID. Minecraft 1.7.10's {@link NBTTagCompound} has no UUID helpers, so the ID is
 * stored as two longs under {@code az_idMost} / {@code az_idLeast}, the same keys later versions' {@code setUniqueId}
 * uses.
 */
public final class AzItemIds {

    private static final String MOST = AzureLib.ITEM_UUID_TAG + "Most";

    private static final String LEAST = AzureLib.ITEM_UUID_TAG + "Least";

    private AzItemIds() {
        throw new UnsupportedOperationException();
    }

    public static boolean has(@Nullable NBTTagCompound tag) {
        // 4 = TAG_Long
        return tag != null && tag.hasKey(MOST, 4) && tag.hasKey(LEAST, 4);
    }

    public static UUID get(NBTTagCompound tag) {
        return new UUID(tag.getLong(MOST), tag.getLong(LEAST));
    }

    public static void set(NBTTagCompound tag, UUID id) {
        tag.setLong(MOST, id.getMostSignificantBits());
        tag.setLong(LEAST, id.getLeastSignificantBits());
    }

    public static boolean has(@Nullable ItemStack stack) {
        return stack != null && has(stack.getTagCompound());
    }

    /** The stack's ID, or {@code null} if it has none. */
    @Nullable
    public static UUID getOrNull(@Nullable ItemStack stack) {
        return has(stack) ? get(stack.getTagCompound()) : null;
    }
}
