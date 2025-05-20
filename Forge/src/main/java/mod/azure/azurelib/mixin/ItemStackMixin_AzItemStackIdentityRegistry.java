package mod.azure.azurelib.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * A mixin class for injecting additional functionality into the {@link ItemStack} constructor to handle identity
 * registration via AzureLib. This mixin ensures that every {@link ItemStack} is assigned a unique identifier when its
 * corresponding item has been registered in the {@link AzIdentityRegistry} and no existing UUID is present in the
 * item's {@link CompoundNBT}. The mixin method `az_addIdentityComponent` is invoked at the "TAIL" of the
 * {@link ItemStack} constructor, which takes a {@link CompoundNBT} as a parameter.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    /**
     * Injects into the constructor of the {@link ItemStack} that takes a {@link CompoundNBT} parameter to initialize a
     * unique AzureLib ID (Az ID) if the item is registered in the {@link AzIdentityRegistry}.
     *
     * @param compoundTag The {@link CompoundNBT} associated with the {@link ItemStack}.
     * @param ci          The {@link CallbackInfo} for the mixin injection.
     */

    @Inject(
        method = "<init>(Lnet/minecraft/nbt/CompoundNBT;)V",
        at = @At("TAIL")
    )
    public void azurelib$initializeAzIdFromCompoundTag(CompoundNBT compoundTag, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, compoundTag);
    }

    /**
     * Injects into the constructor of the {@link ItemStack} that takes an {@link IItemProvider}, an integer item count,
     * and an {@link Optional} for the compound tag. This ensures that a unique AzureLib ID (Az ID) is initialized if
     * the item is registered in {@link AzIdentityRegistry}.
     *
     * @param ci The {@link CallbackInfo} for the mixin injection.
     */
    @Inject(method = "<init>(Lnet/minecraft/util/IItemProvider;ILjava/util/Optional;)V", at = @At("TAIL"))
    public void azurelib$initializeAzIdForConstructorWithOptional(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    /**
     * Injects into the constructor of the {@link ItemStack} that takes an {@link IItemProvider} and an integer count. This
     * ensures that a unique AzureLib ID (Az ID) is initialized if the item is registered in {@link AzIdentityRegistry}.
     *
     * @param ci The {@link CallbackInfo} for the mixin injection.
     */
    @Inject(
        method = "<init>(Lnet/minecraft/util/IItemProvider;)V", at = @At("TAIL")
    )
    public void azurelib$initializeAzIdForConstructor(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    /**
     * Ensures that a unique AzureLib ID (Az ID) is initialized on the provided stack object if the item it represents
     * is registered in the {@link AzIdentityRegistry} and does not already have a unique identifier. If necessary,
     * assigns a new {@link CompoundNBT} for the stack and generates a new UUID.
     *
     * @param stackObject The object representing the stack, expected to be an instance of {@link ItemStack}.
     * @param tag         The {@link CompoundNBT} associated with the stack, used for storing or retrieving data.
     */
    @Unique
    private void azureLib$initializeAzIdOnStack(Object stackObject, CompoundNBT tag) {
        ItemStack self = AzureLibUtil.<ItemStack>self(stackObject);

        if (!AzIdentityRegistry.hasIdentity(self.getItem())) {
            return;
        }

        CompoundNBT stackTag = self.getOrCreateTag();

        if (!stackTag.hasUUID(AzureLib.ITEM_UUID_TAG)) {
            stackTag.putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
    }

}
