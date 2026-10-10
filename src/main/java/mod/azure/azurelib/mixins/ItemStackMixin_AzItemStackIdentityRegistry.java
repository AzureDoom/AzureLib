package mod.azure.azurelib.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Assigns an Az ID to every newly created stack of an AzureLib-identified item. On 1.7.10 every public
 * {@link ItemStack} constructor ends in {@code (Item, int, int)}, and stacks loaded from NBT go through
 * {@code readFromNBT}.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"))
    private void azurelib$initializeAzIdForConstructor(Item item, int size, int meta, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void azurelib$initializeAzIdFromCompoundTag(NBTTagCompound compoundTag, CallbackInfo ci) {
        azureLib$initializeAzIdOnStack(this);
    }

    @Unique
    private static void azureLib$initializeAzIdOnStack(Object stackObject) {
        ItemStack self = AzureLibUtil.self(stackObject);

        // Required due to mods with strict NBT handling; only touch AzureLib-identified items.
        if (self.getItem() == null || !AzIdentityRegistry.hasIdentity(self.getItem())) {
            return;
        }

        NBTTagCompound stackTag = AzureLibUtil.getOrCreateTag(self);

        if (!AzItemIds.has(stackTag)) {
            AzItemIds.set(stackTag, UUID.randomUUID());
        }
    }
}
