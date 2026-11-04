package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.animation.cache.AzItemIdentity;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin_AzIdentityReequip {

    @WrapOperation(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"
        )
    )
    private boolean azurelib$ignoreIdentityOnlyChanges(ItemStack held, ItemStack current, Operation<Boolean> original) {
        return original.call(held, current) || AzItemIdentity.differsOnlyByIdentity(held, current);
    }
}
