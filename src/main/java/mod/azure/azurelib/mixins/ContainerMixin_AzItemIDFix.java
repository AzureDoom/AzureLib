package mod.azure.azurelib.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.cache.AzItemIdentity;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Gives a creative "pick block" (middle click, click mode 3) copy of an AzureLib-identified stack its own Az ID, so the
 * clone doesn't share an animator with the original.
 */
@Mixin(Container.class)
public abstract class ContainerMixin_AzItemIDFix {

    private static final int CLONE_MODE = 3;

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void azurelib$clearCloneId(
        int slotId,
        int clickedButton,
        int mode,
        EntityPlayer player,
        CallbackInfoReturnable<ItemStack> cir
    ) {
        if (mode != CLONE_MODE || player == null) {
            return;
        }

        ItemStack held = player.inventory.getItemStack();

        if (AzureLibUtil.isEmpty(held) || !AzIdentityRegistry.hasIdentity(held.getItem())) {
            return;
        }

        AzItemIdentity.clear(held);
    }
}
