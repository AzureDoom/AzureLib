package mod.azure.azurelib.common.internal.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;

/**
 * A Mixin extension for the `AbstractContainerMenu` class that introduces support for AzureLib-specific ItemStack
 * identity management (Az ID). This Mixin ensures the proper handling, synchronization, and comparison of
 * AzureLib-registered item stacks with custom identifiers during container interactions.
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin_AzItemIDFix {

    @Unique
    private static final int DEFAULT_AZ_ID = -1;

    /**
     * Removes the AzureLib-specific ID (Az ID) from a copied `ItemStack` during a container click action. This is only
     * performed if the original stack's item is registered with AzureLib's identity registry.
     * <p>
     * Tooltip: Prevents the propagation of the Az ID when `ItemStack` objects are copied during container interactions,
     * keeping custom IDs only for registered items.
     */
    @WrapOperation(
        method = "doClick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;",
            ordinal = 1
        )
    )
    public ItemStack azurelib$syncAzureIDWithRemote(ItemStack itemStack, int count, Operation<ItemStack> original) {
        var copyStack = original.call(itemStack, count);

        if (AzIdentityRegistry.hasIdentity(itemStack.getItem()) && copyStack.has(AzureLib.AZ_ID.get())) {
            copyStack.remove(AzureLib.AZ_ID.get());
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
     * @param original            The original operation to call for the base comparison.
     * @return True if the base comparison is true and the Az IDs (if present) match; false otherwise.
     */
    @Unique
    private boolean azurelib$compareStacksWithAzureID(
        ItemStack itemStack,
        ItemStack comparisonItemStack,
        Operation<Boolean> original
    ) {
        if (AzIdentityRegistry.hasIdentity(itemStack.getItem())) {
            return original.call(itemStack, comparisonItemStack) && azurelib$stacksHaveMatchingAzID(
                itemStack,
                comparisonItemStack
            );
        }
        return original.call(itemStack, comparisonItemStack);
    }

    /**
     * Checks if the Az IDs of two `ItemStack` objects match.
     *
     * @param itemStack           The first ItemStack.
     * @param comparisonItemStack The second ItemStack.
     * @return True if the Az IDs match, false otherwise.
     */
    @Unique
    private boolean azurelib$stacksHaveMatchingAzID(ItemStack itemStack, ItemStack comparisonItemStack) {
        return itemStack.getOrDefault(AzureLib.AZ_ID.get(), DEFAULT_AZ_ID)
            .equals(comparisonItemStack.getOrDefault(AzureLib.AZ_ID.get(), DEFAULT_AZ_ID));
    }

}
