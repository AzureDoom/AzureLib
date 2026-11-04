package mod.azure.azurelib.common.internal.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;

/**
 * Ensures stacks duplicated by container clicks (creative clone, quick-craft distribution) receive their own Az ID, so
 * they never share an animator with the stack they were copied from.
 * <p>
 * No slot-sync comparison hooks are needed: {@code ItemStack.matches} already compares all data components, including
 * {@link AzureLib#AZ_ID}.
 */
@Mixin(AbstractContainerMenu.class)
public class AbstractContainerMenuMixin_AzItemIDFix {

    @WrapOperation(
        method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack azurelib$assignFreshAzIdToCopies(ItemStack itemStack, int count, Operation<ItemStack> original) {
        var copy = original.call(itemStack, count);

        if (!copy.isEmpty() && AzIdentityRegistry.hasIdentity(copy.getItem())) {
            copy.set(AzureLib.AZ_ID.get(), UUID.randomUUID());
        }

        return copy;
    }
}
