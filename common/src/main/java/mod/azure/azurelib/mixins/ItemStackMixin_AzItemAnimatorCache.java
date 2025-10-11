package mod.azure.azurelib.mixins;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<UUID, ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<UUID, ItemStack> animator) {
        var itemStack = AzureLibUtil.<ItemStack>self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance().add(itemStack, (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<UUID, ItemStack> getAnimatorOrNull() {
        var self = AzureLibUtil.<ItemStack>self(this);
        var tag = self.getTag();
        if (tag == null || !tag.contains(AzureLib.ITEM_UUID_TAG)) {
            return null;
        }

        var uuid = tag.getUUID(AzureLib.ITEM_UUID_TAG);
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
