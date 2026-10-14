package mod.azure.azurelib.mixins;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;
import javax.annotation.Nullable;

import mod.azure.azurelib.animation.AzAnimator;
import mod.azure.azurelib.animation.AzAnimatorAccessor;
import mod.azure.azurelib.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.animation.impl.AzItemAnimator;
import mod.azure.azurelib.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Item stacks are recreated freely (copies, network updates), so their animators are not stored on the stack itself but
 * in {@link AzIdentifiableItemStackAnimatorCache}, keyed by the stack's AzureLib UUID.
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<UUID, ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<UUID, ItemStack> animator) {
        ItemStack itemStack = AzureLibUtil.self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance()
            .add(itemStack, AzItemAnimatorView.current(), (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<UUID, ItemStack> getAnimatorOrNull() {
        UUID uuid = AzItemIds.getOrNull(AzureLibUtil.self(this));
        return uuid == null
            ? null
            : AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid, AzItemAnimatorView.current());
    }
}
