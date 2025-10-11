package mod.azure.azurelib.mixins;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * A mixin class for augmenting the behavior of the {@code ItemStack} class, specifically to support the initialization
 * of unique identifiers for item stacks via AzureLib integration. This enables compatibility with custom rendering and
 * other features relying on unique item identifiers.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(
        method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V",
        at = @At("TAIL")
    )
    public void azurelib$initializeAzIdFromCompoundTag(CompoundTag compoundTag, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, compoundTag);
    }

    @Inject(
        method = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;ILjava/util/Optional;)V",
        at = @At("TAIL")
    )
    public void azurelib$initializeAzIdForConstructorWithOptional(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    @Inject(
        method = "Lnet/minecraft/world/item/ItemStack;<init>(Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL")
    )
    public void azurelib$initializeAzIdForConstructor(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this, null);
    }

    /**
     * Initializes a unique identifier for an ItemStack on the stack if applicable. If the ItemStack has associated item
     * or armor renderers, this method ensures that it has a unique UUID stored in its tag.
     *
     * @param stackObject The object representing the ItemStack instance. This is expected to be compatible with
     *                    {@code ItemStack}.
     * @param tag         The CompoundTag containing data for the stack, or {@code null}. If {@code null}, a new tag or
     *                    the existing tag associated with the stack will be used.
     */
    @Unique
    private void azureLib$initializeAzIdOnStack(Object stackObject, CompoundTag tag) {
        var self = AzureLibUtil.<ItemStack>self(stackObject);

        // Required due to stupid mods like Occultism, and its stupid strict tags
        if (!AzIdentityRegistry.hasIdentity(self.getItem())) {
            return;
        }
        var stackTag = self.getOrCreateTag();

        if (AzIdentityRegistry.hasIdentity(self.getItem()) && !stackTag.hasUUID(AzureLib.ITEM_UUID_TAG)) {
            stackTag.putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
    }

}
