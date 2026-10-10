package mod.azure.azurelib.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;

/**
 * Gives a creative "pick block" ({@link ClickType#CLONE}) copy of an AzureLib-identified stack its own Az ID, so the
 * clone doesn't share an animator with the original.
 * <p>
 * The 1.18 branch also wraps the slot-sync stack comparisons; on 1.12.2 {@code ItemStack.areItemStacksEqual} already
 * compares the full NBT (which contains the Az ID), so those wrappers aren't needed.
 */
@Mixin(Container.class)
public abstract class ContainerMixin_AzItemIDFix {

    @Inject(method = "slotClick", at = @At("RETURN"))
    private void azurelib$reassignCloneId(
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

        if (held.isEmpty() || !AzIdentityRegistry.hasIdentity(held.getItem()) || !held.hasTagCompound()) {
            return;
        }

        held.getTagCompound().setUniqueId(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
    }
}
