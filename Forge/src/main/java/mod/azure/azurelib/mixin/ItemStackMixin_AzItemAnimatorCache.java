package mod.azure.azurelib.mixin;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.rewrite.animation.AzAnimator;
import mod.azure.azurelib.rewrite.animation.AzAnimatorAccessor;
import mod.azure.azurelib.rewrite.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.rewrite.animation.impl.AzItemAnimator;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<ItemStack> animator) {
        var itemStack = AzureLibUtil.<ItemStack>self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance().add(itemStack, (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<ItemStack> getAnimatorOrNull() {
        var self = AzureLibUtil.<ItemStack>self(this);
        var uuid = self.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG);
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
