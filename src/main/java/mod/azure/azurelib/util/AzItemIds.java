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
 * Beta stacks have no NBT, so the UUID lives in the AzureLib data compound that
 * {@link mod.azure.azurelib.mixins.ItemStackMixin_AzItemStackData} adds to every stack. It is saved with the stack and
 * carried over by {@code copy()}, but vanilla doesn't sync it: multiplayer clients adopt the server's id from
 * AzureLib's item dispatch packet.
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
        return stack != null && has(Mc.stackNbtOrNull(stack));
    }

    @Nullable
    public static UUID getOrNull(@Nullable ItemStack stack) {
        NbtCompound tag = stack == null ? null : Mc.stackNbtOrNull(stack);
        return has(tag) ? get(tag) : null;
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

    /**
     * Copies an AzureLib data compound for {@code ItemStack#copy()}. Beta's NBT has no generic deep copy, and the
     * compound currently holds only the identity UUID, so that is what gets copied.
     */
    public static NbtCompound copyData(NbtCompound from) {
        NbtCompound copy = new NbtCompound();
        if (has(from)) {
            set(copy, get(from));
        }
        return copy;
    }
}
