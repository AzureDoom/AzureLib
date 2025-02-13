package mod.azure.azurelib.mixins;

import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * A mixin class for modifying the initialization behavior of the {@link ItemStack} class. This mixin specifically
 * ensures that a unique identity component is added to an {@link ItemStack} upon creation, provided that the associated
 * item has been registered in the {@link AzIdentityRegistry}. When an {@link ItemStack} is instantiated, the mixin
 * checks if: - The item has an identity registered in {@link AzIdentityRegistry}. - The provided
 * {@link PatchedDataComponentMap} does not already contain an `az_id` component. If both conditions are met, the mixin
 * assigns a universally unique identifier (UUID) as the `az_id` component to the associated
 * {@link PatchedDataComponentMap}. This mechanism enables unique identification and tracking of specific item stacks in
 * the game.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(
        method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    public void az_addIdentityComponent(CompoundTag compoundTag, CallbackInfo ci) {
        var self = AzureLibUtil.<ItemStack>self(this);
        if (AzIdentityRegistry.hasIdentity(self.getItem()) && !compoundTag.hasUUID("az_id")) {
            compoundTag.putUUID("az_id", UUID.randomUUID());
        }
    }
}
