package mod.azure.azurelib.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.platform.mc.Mc;

/**
 * Reads and writes the AzureLib identity UUID stored on item stacks.
 * <p>
 * On Beta the UUID lives in StationAPI's per-stack NBT ({@code StationItemStack#getStationNbt()}), which StationAPI
 * persists and syncs to clients.
 */
public final class AzItemIds {

    private static final String MOST = AzureLib.ITEM_UUID_TAG + "Most";

    private static final String LEAST = AzureLib.ITEM_UUID_TAG + "Least";

    private AzItemIds() {
        throw new UnsupportedOperationException();
    }

    public static boolean has(@Nullable NbtCompound tag) {
        return tag != null && Mc.nbtHas(tag, MOST) && Mc.nbtHas(tag, LEAST);
    }

    public static UUID get(NbtCompound tag) {
        return new UUID(Mc.nbtGetLong(tag, MOST), Mc.nbtGetLong(tag, LEAST));
    }

    public static void set(NbtCompound tag, UUID id) {
        Mc.nbtPutLong(tag, MOST, id.getMostSignificantBits());
        Mc.nbtPutLong(tag, LEAST, id.getLeastSignificantBits());
    }

    public static boolean has(@Nullable ItemStack stack) {
        return stack != null && has(Mc.stackNbt(stack));
    }

    @Nullable
    public static UUID getOrNull(@Nullable ItemStack stack) {
        return has(stack) ? get(Mc.stackNbt(stack)) : null;
    }

    /** Returns the stack's UUID, assigning a fresh random one first if it has none. */
    public static UUID getOrCreate(ItemStack stack) {
        NbtCompound tag = Mc.stackNbt(stack);
        if (!has(tag)) {
            set(tag, UUID.randomUUID());
        }
        return get(tag);
    }

    public static void set(ItemStack stack, UUID id) {
        set(Mc.stackNbt(stack), id);
    }
}
