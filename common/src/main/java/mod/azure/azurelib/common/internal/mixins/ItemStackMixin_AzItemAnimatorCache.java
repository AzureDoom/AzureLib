package mod.azure.azurelib.common.internal.mixins;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.common.animation.AzAnimator;
import mod.azure.azurelib.common.animation.AzAnimatorAccessor;
import mod.azure.azurelib.common.animation.cache.AzIdentifiableItemStackAnimatorCache;
import mod.azure.azurelib.common.animation.impl.AzItemAnimator;
import mod.azure.azurelib.common.animation.impl.AzItemAnimatorView;
import mod.azure.azurelib.common.util.AzureLibUtil;

/**
 * Stores and looks up item stack animators by stack UUID, for the {@link AzItemAnimatorView} currently being rendered
 * on this thread ({@link AzItemAnimatorView#DEFAULT} outside of rendering).
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin_AzItemAnimatorCache implements AzAnimatorAccessor<UUID, ItemStack> {

    @Override
    public void setAnimator(@Nullable AzAnimator<UUID, ItemStack> animator) {
        var itemStack = AzureLibUtil.<ItemStack>self(this);
        AzIdentifiableItemStackAnimatorCache.getInstance()
            .add(itemStack, AzItemAnimatorView.current(), (AzItemAnimator) animator);
    }

    @Override
    public @Nullable AzAnimator<UUID, ItemStack> getAnimatorOrNull() {
        var self = AzureLibUtil.<ItemStack>self(this);
        var uuid = self.getComponents().get(AzureLib.AZ_ID.get());
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid, AzItemAnimatorView.current());
    }
}
