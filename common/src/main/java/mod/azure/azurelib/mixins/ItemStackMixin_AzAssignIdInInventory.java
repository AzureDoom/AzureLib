package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
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
 * <p>
 * The target is matched by name with the entity captured through {@code @Local(argsOnly = true)}, so the hook doesn't
 * depend on the rest of {@code inventoryTick}'s parameter list (its slot parameter has changed shape between versions).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzAssignIdInInventory {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void azurelib$assignIdWhileInInventory(
        CallbackInfo ci,
        @Local(argsOnly = true, name = "owner") Entity entity
    ) {
        if (entity == null || entity.level().isClientSide()) {
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
