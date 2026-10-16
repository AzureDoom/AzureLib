package mod.azure.azurelib.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.cache.AzItemIdentity;

/**
 * Strips the copied Az ID from a creative "pick block" ({@link ClickType#CLONE}) copy of an AzureLib-identified stack,
 * so the clone doesn't share an animator with the original. Ids are assigned lazily, so the clone gets its own the
 * first time it's dispatched to, and until then it's equal to a freshly created stack.
 * <p>
 * The 1.18 branch also wraps the slot-sync stack comparisons; on 1.12.2 {@code ItemStack.areItemStacksEqual} already
 * compares the full NBT (which contains the Az ID), so those wrappers aren't needed.
 */
@Mixin(Container.class)
public abstract class ContainerMixin_AzItemIDFix {

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void azurelib$clearCloneId(
        int slotId,
        int dragType,
        ClickType clickType,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (clickType != ClickType.CLONE || player == null) {
            return;
        }

        ItemStack held = player.inventory.getItemStack();

        if (held.isEmpty() || !AzIdentityRegistry.hasIdentity(held.getItem())) {
            return;
        }

        AzItemIdentity.clear(held);
    }
}
