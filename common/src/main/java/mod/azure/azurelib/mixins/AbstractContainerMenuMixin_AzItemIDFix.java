package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;

/**
 * Ensures stacks duplicated by container clicks receive their own Az ID, so they never share an animator with the stack
 * they were copied from. {@code ItemStack.matches} already compares the full tag (including the Az ID), so no slot-sync
 * comparison hooks are needed.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

    @WrapOperation(
        method = "doClick",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", ordinal = 1
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", ordinal = 2
            )
        }
    )
    private ItemStack azurelib$assignFreshAzIdToCopy(ItemStack itemStack, Operation<ItemStack> original) {
        var copy = original.call(itemStack);

        if (!copy.isEmpty() && AzIdentityRegistry.hasIdentity(copy.getItem())) {
            copy.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }

        return copy;
    }
}
