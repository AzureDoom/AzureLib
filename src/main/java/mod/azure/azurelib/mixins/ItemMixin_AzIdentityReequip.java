package mod.azure.azurelib.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.animation.cache.AzItemIdentity;

/**
 * Az IDs are assigned lazily, on the first server-side dispatch, which changes the held stack's NBT and would make
 * vanilla play the re-equip animation once (usually right as the first triggered animation starts). A change that only
 * adds or swaps the Az ID is not treated as a re-equip. Items that override {@code shouldCauseReequipAnimation}
 * themselves are unaffected.
 */
@Mixin(Item.class)
public abstract class ItemMixin_AzIdentityReequip {

    @Inject(method = "shouldCauseReequipAnimation", at = @At("HEAD"), cancellable = true, remap = false)
    private void azurelib$ignoreIdentityOnlyChanges(
        ItemStack oldStack,
        ItemStack newStack,
        boolean slotChanged,
        CallbackInfoReturnable<Boolean> cir
    ) {
        if (!slotChanged && AzItemIdentity.differsOnlyByIdentity(oldStack, newStack)) {
            cir.setReturnValue(false);
        }
    }
}
