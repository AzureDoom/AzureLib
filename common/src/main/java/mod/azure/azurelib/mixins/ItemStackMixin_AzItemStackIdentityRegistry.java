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
 * A mixin class for injecting additional functionality into the {@link ItemStack}
 * constructor to handle identity registration via AzureLib.
 *
 * This mixin ensures that every {@link ItemStack} is assigned a unique identifier
 * when its corresponding item has been registered in the {@link AzIdentityRegistry} and
 * no existing UUID is present in the item's {@link CompoundTag}.
 *
 * The mixin method `az_addIdentityComponent` is invoked at the "TAIL" of the
 * {@link ItemStack} constructor, which takes a {@link CompoundTag} as a parameter.
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
