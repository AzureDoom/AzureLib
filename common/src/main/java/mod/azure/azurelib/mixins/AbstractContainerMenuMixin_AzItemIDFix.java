package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.cache.AzItemIdentity;

/**
 * Strips the copied Az ID from stacks that {@code doClick} duplicates, so a copy never shares an animator with its
 * source. Ids are assigned lazily, so each copy gets its own once it ticks in an inventory on the server, and until
 * then it's equal to a freshly created stack.
 * <ul>
 * <li>{@code Slot#safeClone}: creative pick-block ({@code ContainerInput.CLONE}).</li>
 * <li>{@code ItemStack#copyWithCount}: the per-slot copies of a quick-craft drag, including the creative middle-drag,
 * which copies full stacks and so duplicates non-stackable items too. The carried remainder is a {@code copy()} and
 * keeps its id.</li>
 * </ul>
 * No slot-sync comparison hooks are needed: {@code ItemStack.matches} already compares the Az ID.
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin_AzItemIDFix {

    @WrapOperation(
        method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/inventory/Slot;safeClone(Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack azurelib$clearCloneId(Slot slot, Player player, Operation<ItemStack> original) {
        return azurelib$withoutAzId(original.call(slot, player));
    }

    @WrapOperation(
        method = "doClick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;copyWithCount(I)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack azurelib$clearDragCopyId(ItemStack source, int count, Operation<ItemStack> original) {
        return azurelib$withoutAzId(original.call(source, count));
    }

    @Unique
    private static ItemStack azurelib$withoutAzId(ItemStack copy) {
        if (!copy.isEmpty() && AzIdentityRegistry.hasIdentity(copy.getItem())) {
            AzItemIdentity.clear(copy);
        }

        return copy;
    }
}
