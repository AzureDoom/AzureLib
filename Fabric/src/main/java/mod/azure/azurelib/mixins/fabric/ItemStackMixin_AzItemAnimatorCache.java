package mod.azure.azurelib.mixins.fabric;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
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
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<ItemStack> animator) {
        ItemStack itemStack = AzureLibUtil.<ItemStack>self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance().add(itemStack, (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<ItemStack> getAnimatorOrNull() {
        ItemStack self = AzureLibUtil.<ItemStack>self(this);

        if (!AzIdentityRegistry.hasIdentity(self.getItem())) {
            return null;
        }

        UUID uuid = self.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG);
        if (!self.getOrCreateTag().contains(AzureLib.ITEM_UUID_TAG)) {
            self.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
        }
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
