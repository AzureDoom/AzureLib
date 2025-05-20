package mod.azure.azurelib.mixin;

import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;

/**
 * A Mixin extension for the {@code AbstractContainerMenu} class that introduces support for AzureLib-specific
 * {@code ItemStack} identity management (Az ID). This Mixin ensures the proper handling, synchronization, and
 * comparison of AzureLib-registered item stacks with custom identifiers during container interactions.
 */
@Mixin(Container.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

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
            target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;",
            ordinal = 1
        )
    )
    public ItemStack azurelib$syncAzureIDWithRemote(ItemStack itemStack, int count) {
        ItemStack copyStack = itemStack.copy();

        copyStack.setCount(itemStack.getCount());

        if (AzIdentityRegistry.hasIdentity(itemStack.getItem()) && copyStack.hasTag()) {
            copyStack.getOrCreateTag().remove(AzureLib.ITEM_UUID_TAG);
            copyStack.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }

        return copyStack;
    }

}
