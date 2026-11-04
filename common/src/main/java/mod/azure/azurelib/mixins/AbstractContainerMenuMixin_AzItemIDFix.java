package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.cache.AzItemIdentity;

/**
 * Strips the copied Az ID from stacks that {@code doClick} duplicates, so a copy never shares an animator with its
 * source. Ids are assigned lazily, so each copy gets its own once it ticks in an inventory on the server, and until
 * then it's equal to a freshly created stack.
 * <p>
 * No slot-sync comparison hooks are needed: {@code ItemStack.matches} already compares the full tag, including the Az
 * ID.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

    @WrapOperation(
        method = "doClick",
        at = {
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
                ordinal = 1
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;",
                ordinal = 2
            )
        }
    )
    private ItemStack azurelib$clearCopiedAzId(ItemStack source, Operation<ItemStack> original) {
        var copy = original.call(source);

        if (!copy.isEmpty() && AzIdentityRegistry.hasIdentity(copy.getItem())) {
            AzItemIdentity.clear(copy);
        }

        return copy;
    }
}
