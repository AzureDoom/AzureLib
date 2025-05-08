package mod.azure.azurelib.mixin;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

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
        UUID uuid = self.getOrCreateTag().getUUID(AzureLib.ITEM_UUID_TAG);
        return AzIdentifiableItemStackAnimatorCache.getInstance().getOrNull(uuid);
    }
}
