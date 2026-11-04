package mod.azure.azurelib.mixins;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
 * its first use. Stacks that never sit in an inventory (creative tabs, recipe outputs, JEI/EMI) still get no id.
 * <p>
 * Hooks {@code ItemStack#inventoryTick} rather than {@code Item#inventoryTick}, since items commonly override the
 * latter without calling super.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzAssignIdInInventory {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void azurelib$assignIdWhileInInventory(
        Level level,
        Entity entity,
        int inventorySlot,
        boolean isCurrentItem,
        CallbackInfo ci
    ) {
        if (level == null || level.isClientSide()) {
            return;
        }

        var self = AzureLibUtil.<ItemStack>self(this);

        if (
            !self.isEmpty() && AzIdentityRegistry.hasIdentity(self.getItem()) && !AzItemIdentity.hasPersistentId(self)
        ) {
            AzItemIdentity.getOrCreate(self);
        }
    }
}
