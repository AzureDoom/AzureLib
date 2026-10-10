package mod.azure.azurelib.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * Helper class for various methods and functions useful while using AzureLib.
 */
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
     * Returns the stack's NBT compound, creating and attaching an empty one if absent. 1.7.10 has no
     * {@code ItemStack#getOrCreateTag()}.
     */
    public static NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            stack.setTagCompound(tag);
        }
        return tag;
    }

    /**
     * Minecraft 1.7.10 represents an empty slot as a {@code null} stack. This also treats stacks with no item or a
     * non-positive size as empty, like later versions' {@code ItemStack#isEmpty()}.
     */
    public static boolean isEmpty(@Nullable ItemStack stack) {
        return stack == null || stack.getItem() == null || stack.stackSize <= 0;
    }
}
