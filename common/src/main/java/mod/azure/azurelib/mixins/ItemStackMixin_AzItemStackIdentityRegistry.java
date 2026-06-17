package mod.azure.azurelib.mixins;

import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * This mixin modifies the {@link ItemStack} class to inject functionality for managing a unique identifier as part of
 * the ItemStack's data components. The added identifier is used in conjunction with the animation registry provided by
 * AzureLib.
 * <p>
 * When an {@code ItemStack} is instantiated and is associated with an animatable item, this mixin ensures that it
 * includes a unique identifier in its data components. If the item is animatable and the required data component is not
 * yet present, it assigns a newly generated {@link UUID} to the component.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(
        method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
        at = @At("TAIL")
    )
    public void az_addIdentityComponent(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        var self = AzureLibUtil.<ItemStack>self(this);

        if (AzIdentityRegistry.hasIdentity(self.getItem()) && !components.has(AzureLib.AZ_ID.get())) {
            components.set(AzureLib.AZ_ID.get(), UUID.randomUUID());
        }
    }
}
