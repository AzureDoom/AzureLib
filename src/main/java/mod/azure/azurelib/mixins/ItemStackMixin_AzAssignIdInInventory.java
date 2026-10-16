package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.animation.cache.AzItemIdentity;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Gives AzureLib-identified stacks their persistent Az ID on the logical server as soon as they tick in an entity's
 * inventory, so the id (and the slot sync that carries it) arrives while the item is idle rather than in the middle of
 * its first use. Stacks that never sit in an inventory (creative tabs, recipe outputs, JEI) still get no id.
 * <p>
 * Hooks {@code ItemStack#updateAnimation} rather than {@code Item#onUpdate}, since items commonly override
 * {@code onUpdate} without calling super.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzAssignIdInInventory {

    @Inject(method = "updateAnimation", at = @At("HEAD"))
    private void azurelib$assignIdWhileInInventory(
        World world,
        Entity entity,
        int inventorySlot,
        boolean isCurrentItem,
        CallbackInfo ci
    ) {
        if (world == null || world.isRemote) {
            return;
        }

        ItemStack self = AzureLibUtil.self(this);

        if (
            !AzureLibUtil.isEmpty(self) && AzIdentityRegistry.hasIdentity(self.getItem()) && !AzItemIdentity
                .hasPersistentId(self)
        ) {
            AzItemIdentity.getOrCreate(self);
        }
    }
}
