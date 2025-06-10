package mod.azure.azurelib.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<ItemStack> {

    @Override
    public void setAnimator(AzAnimator<ItemStack> animator) {
        ItemStack itemStack = AzureLibUtil.<ItemStack>self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance().add(itemStack, (AzItemAnimator) animator);
    }

    @Override
    public AzAnimator<ItemStack> getAnimatorOrNull() {
        ItemStack self = AzureLibUtil.<ItemStack>self(this);
        UUID uuid = self.getOrCreateTag().getUniqueId(AzureLib.ITEM_UUID_TAG);
        if (!self.getOrCreateTag().contains(AzureLib.ITEM_UUID_TAG)) {
            self.getOrCreateTag().putUniqueId(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
