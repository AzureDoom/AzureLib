package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;

/**
 * A Mixin extension for the {@code AbstractContainerMenu} class that introduces support for AzureLib-specific
 * {@code ItemStack} identity management (Az ID). This Mixin ensures the proper handling, synchronization, and
 * comparison of AzureLib-registered item stacks with custom identifiers during container interactions.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

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
            target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
            ordinal = 1
        )
    )
    public ItemStack azurelib$syncAzureIDWithRemote(ItemStack itemStack, int count) {
        var copyStack = itemStack.copyWithCount(count);

        if (AzIdentityRegistry.hasIdentity(itemStack.getItem()) && copyStack.hasTag()) {
            copyStack.getOrCreateTag().remove(AzureLib.ITEM_UUID_TAG);
            copyStack.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }

        return copyStack;
    }

    /**
     * Validates AzureLib-specific IDs (Az ID) between two `ItemStack` objects during remote slot synchronization. This
     * ensures that items with custom IDs remain synchronized correctly during slot operations.
     * <p>
     * Tooltip: Compares two `ItemStack` objects, ensuring their Az IDs (if present) also match.
     */
    @WrapOperation(
        method = "synchronizeSlotToRemote", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    public boolean azurelib$syncAzureIDWithRemote(
        ItemStack itemStack,
        ItemStack comparisonItemStack,
        Operation<Boolean> original
    ) {
        return azurelib$compareStacksWithAzureID(itemStack, comparisonItemStack, original);
    }

    /**
     * Forces an Az ID comparison when listening for slot changes involving two `ItemStack` objects.
     * <p>
     * Tooltip: Ensures that slot listeners detect changes in AzureLib-registered item stacks based not only on their
     * normal properties but also their Az ID values, if applicable.
     */
    @WrapOperation(
        method = "triggerSlotListeners", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    public boolean azurelib$detectSlotChangeWithAzureID(
        ItemStack itemStack,
        ItemStack comparisonItemStack,
        Operation<Boolean> original
    ) {
        return azurelib$compareStacksWithAzureID(itemStack, comparisonItemStack, original);
    }

    /**
     * Compares two ItemStacks while considering AzureLib-specific IDs (Az ID) if the item is registered with AzureLib.
     *
     * @param itemStack           The first ItemStack to compare.
     * @param comparisonItemStack The second ItemStack to compare.
     * @return True if the base comparison is true and the Az IDs (if present) match; false otherwise.
     */
    @Unique
    private boolean azurelib$compareStacksWithAzureID(
        ItemStack itemStack,
        ItemStack comparisonItemStack,
        Operation<Boolean> original
    ) {
        if (AzIdentityRegistry.hasIdentity(itemStack.getItem())) {
            return original.call(itemStack, comparisonItemStack);
        }

        return original.call(itemStack, comparisonItemStack) && azurelib$checkAzIDMatch(
            itemStack.getTag(),
            comparisonItemStack.getTag()
        );
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
        return (tag1 == null ? DEFAULT_AZ_ID : tag1.getInt(AzureLib.ITEM_UUID_TAG)) == (tag2 == null
            ? DEFAULT_AZ_ID
            : tag2.getInt(AzureLib.ITEM_UUID_TAG));
    }

}
