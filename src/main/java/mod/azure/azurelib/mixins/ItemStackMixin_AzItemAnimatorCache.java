package mod.azure.azurelib.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<UUID, ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<UUID, ItemStack> animator) {
        ItemStack itemStack = AzureLibUtil.self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance().add(itemStack, (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<UUID, ItemStack> getAnimatorOrNull() {
        ItemStack self = AzureLibUtil.self(this);
        NBTTagCompound tag = self.getTagCompound();

        if (tag == null || !AzItemIds.has(tag)) {
            return null;
        }

        UUID uuid = AzItemIds.get(tag);
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
