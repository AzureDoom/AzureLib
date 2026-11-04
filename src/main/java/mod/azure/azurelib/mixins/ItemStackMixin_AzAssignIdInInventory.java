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
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzAssignIdInInventory {

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    private void azurelib$assignIdWhileInInventory(
        World world,
        Entity entity,
        int slot,
        boolean selected,
        CallbackInfo ci
    ) {
        if (world == null || Mc.isClientSide(world)) {
            return;
        }

        ItemStack self = AzureLibUtil.self(this);

        if (AzIdentityRegistry.hasIdentity(Mc.item(self)) && !AzItemIdentity.hasPersistentId(self)) {
            AzItemIdentity.getOrCreate(self);
        }
    }
}
