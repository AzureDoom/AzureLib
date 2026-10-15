package mod.azure.azurelib.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

/**
 * Helper class for various methods and functions useful while using AzureLib.
 */
@SuppressWarnings("unused")
public final class AzureLibUtil {

    private AzureLibUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * Cast the given object to the type of the receiver. Primarily used in mixins, where the compiler can't know that
     * the mixin is the target class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T self(Object object) {
        return (T) object;
    }

    public static boolean isMultipleOf(int value, int divisor) {
        return value % divisor == 0;
    }

    /**
     * Returns the stack's AzureLib data compound, creating it if needed.
     * <p>
     * Beta stacks have no NBT of their own; this is the compound added by
     * {@link mod.azure.azurelib.mixins.ItemStackMixin_AzItemStackData}.
     */
    public static NbtCompound getOrCreateTag(ItemStack stack) {
        return Mc.stackNbt(stack);
    }

    /**
     * Minecraft Beta represents an empty slot as a {@code null} stack. This also treats stacks with no item or a
     * non-positive size as empty, like later versions' {@code ItemStack#isEmpty()}.
     */
    public static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || Mc.item(stack) == null || Mc.count(stack) <= 0;
    }
}
