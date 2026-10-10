package mod.azure.azurelib.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
 * Assigns an Az ID to every newly created stack of an AzureLib-identified item. Every 1.12.2 {@link ItemStack}
 * constructor ends in either the NBT constructor or the {@code (Item, int, int, NBTTagCompound)} constructor, so those
 * two are covered.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(method = "<init>(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void azurelib$initializeAzIdFromCompoundTag(NBTTagCompound compoundTag, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this);
    }

    @Inject(method = "<init>(Lnet/minecraft/item/Item;IILnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void azurelib$initializeAzIdForConstructor(CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this);
    }

    @Unique
    private static void azureLib$initializeAzIdOnStack(Object stackObject) {
        ItemStack self = AzureLibUtil.self(stackObject);

        // Required due to mods with strict NBT handling; only touch AzureLib-identified items.
        if (self.isEmpty() || !AzIdentityRegistry.hasIdentity(self.getItem())) {
            return;
        }

        NBTTagCompound stackTag = AzureLibUtil.getOrCreateTag(self);

        if (!stackTag.hasUniqueId(AzureLib.ITEM_UUID_TAG)) {
            stackTag.setUniqueId(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
    }
}
