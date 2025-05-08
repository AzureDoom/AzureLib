package mod.azure.azurelib.mixins.fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

/**
 * A Mixin extension for the {@code AbstractContainerMenu} class that introduces support for AzureLib-specific {@code ItemStack}
 * identity management (Az ID). This Mixin ensures the proper handling, synchronization, and comparison of
 * AzureLib-registered item stacks with custom identifiers during container interactions.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

    @Shadow public abstract void removed(Player player);

    @Unique
    private static final int DEFAULT_AZ_ID = -1;

    /**
     * Removes the AzureLib-specific ID (Az ID) from a copied `ItemStack` during a container click action. This is only
     * performed if the original stack's item is registered with AzureLib's identity registry.
     * <p>
     * Tooltip: Prevents the propagation of the Az ID when `ItemStack` objects are copied during container interactions,
     * keeping custom IDs only for registered items.
     */
    @Redirect(
        method = "doClick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
            ordinal = 1
        )
    )
    public ItemStack azurelib$syncAzureIDWithRemote(ItemStack itemStack, int count) {
        ItemStack copyStack = itemStack.copy();

        copyStack.setCount(itemStack.getCount());

        if (AzIdentityRegistry.hasIdentity(itemStack.getItem()) && copyStack.hasTag() && copyStack.getTag().contains(
            AzureLib.ITEM_UUID_TAG)) {
            copyStack.getTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }

        return copyStack;
    }

    /**
     * Compares two ItemStacks while considering AzureLib-specific IDs (Az ID) if the item is registered with AzureLib.
     *
     * @param itemStack           The first ItemStack to compare.
     * @param comparisonItemStack The second ItemStack to compare.
     * @return True if the base comparison is true and the Az IDs (if present) match; false otherwise.
     */
    @Unique
    private boolean azurelib$compareStacksWithAzureID(ItemStack itemStack, ItemStack comparisonItemStack, Operation<Boolean> original) {
        if (!AzIdentityRegistry.hasIdentity(itemStack.getItem())) {
            return original.call(itemStack, comparisonItemStack);
        }

        return original.call(itemStack, comparisonItemStack) && azurelib$checkAzIDMatch(itemStack.getTag(), comparisonItemStack.getTag());
    }

    /**
     * Performs an exclusive-NOR (XNOR) operation on the Az ID tags of two ItemStacks to check for matching IDs.
     *
     * @param tag1 The CompoundTag of the first ItemStack.
     * @param tag2 The CompoundTag of the second ItemStack.
     * @return True if both tags either have matching Az IDs or are null, false otherwise.
     */
    @Unique
    private static boolean azurelib$checkAzIDMatch(CompoundTag tag1, CompoundTag tag2) {
        return (tag1 == null ? DEFAULT_AZ_ID : tag1.getInt(AzureLib.ITEM_UUID_TAG)) == (tag2 == null ? DEFAULT_AZ_ID : tag2.getInt(AzureLib.ITEM_UUID_TAG));
    }

}
