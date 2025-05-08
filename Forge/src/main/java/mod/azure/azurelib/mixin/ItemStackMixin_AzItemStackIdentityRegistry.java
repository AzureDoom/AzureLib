package mod.azure.azurelib.mixin;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
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

    /**
     * Injects into the constructor of the {@link ItemStack} that takes a {@link CompoundTag} parameter to initialize a unique AzureLib ID (Az ID)
     * if the item is registered in the {@link AzIdentityRegistry}.
     *
     * @param compoundTag The {@link CompoundTag} associated with the {@link ItemStack}.
     * @param ci          The {@link CallbackInfo} for the mixin injection.
     */

    @Inject(
        method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    public void azurelib$initializeAzIdFromCompoundTag(CompoundTag compoundTag, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, compoundTag);
    }

    /**
     * Injects into the constructor of the {@link ItemStack} that takes an {@link ItemLike}, an integer item count, and an {@link Optional} for
     * the compound tag. This ensures that a unique AzureLib ID (Az ID) is initialized if the item is registered in {@link AzIdentityRegistry}.
     *
     * @param ci The {@link CallbackInfo} for the mixin injection.
     */
    @Inject(method = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;ILjava/util/Optional;)V", at = @At("TAIL"))
    public void azurelib$initializeAzIdForConstructorWithOptional(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    /**
     * Injects into the constructor of the {@link ItemStack} that takes an {@link ItemLike} and an integer count.
     * This ensures that a unique AzureLib ID (Az ID) is initialized if the item is registered in {@link AzIdentityRegistry}.
     *
     * @param ci The {@link CallbackInfo} for the mixin injection.
     */
    @Inject(method = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    public void azurelib$initializeAzIdForConstructor(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    /**
     * Ensures that a unique AzureLib ID (Az ID) is initialized on the provided stack object if the item it represents
     * is registered in the {@link AzIdentityRegistry} and does not already have a unique identifier. If necessary,
     * assigns a new {@link CompoundTag} for the stack and generates a new UUID.
     *
     * @param stackObject The object representing the stack, expected to be an instance of {@link ItemStack}.
     * @param tag The {@link CompoundTag} associated with the stack, used for storing or retrieving data.
     */
    @Unique
    private void azureLib$initializeAzIdOnStack(Object stackObject, CompoundTag tag) {
        var self = AzureLibUtil.<ItemStack>self(stackObject);

        if (!self.hasTag()) {
            self.setTag(new CompoundTag());
        }

        var stackTag = self.getTag();

        if (stackTag != null && AzIdentityRegistry.hasIdentity(self.getItem()) && !stackTag.hasUUID(
            AzureLib.ITEM_UUID_TAG)) {
            stackTag.putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
    }

}
