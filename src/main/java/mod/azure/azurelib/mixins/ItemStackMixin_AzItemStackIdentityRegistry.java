package mod.azure.azurelib.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.util.AzItemIds;
import mod.azure.azurelib.util.AzureLibUtil;

/**
 * Gives every newly created stack of an {@link AzIdentityRegistry identity-registered} item a UUID in AzureLib's
 * per-stack data (see {@link ItemStackMixin_AzItemStackData}).
 * <p>
 * Stacks are created on both sides. On a multiplayer client the random id assigned here is replaced by the server's the
 * first time an item dispatch packet names the stack, so client and server agree. Stacks loaded from disk go through
 * the NBT constructor instead and keep the id they were saved with.
 * <p>
 * Beta's {@code ItemStack(Block, ...)} and {@code ItemStack(Item, ...)} overloads all funnel into one of the two
 * constructors targeted here; {@code require = 0} because which one is the "real" constructor depends on how the mapped
 * jar delegates.
 */
@Mixin(ItemStack.class)
public class ItemStackMixin_AzItemStackIdentityRegistry {

    @Inject(method = "<init>(Lnet/minecraft/item/Item;II)V", at = @At("RETURN"), require = 0)
    private void azurelib$assignIdFromItem(Item item, int count, int damage, CallbackInfo ci) {
        azurelib$assignId(this);
    }

    @Inject(method = "<init>(III)V", at = @At("RETURN"), require = 0)
    private void azurelib$assignIdFromRawId(int itemId, int count, int damage, CallbackInfo ci) {
        azurelib$assignId(this);
    }

    @Unique
    private static void azurelib$assignId(Object stackObject) {
        ItemStack self = AzureLibUtil.self(stackObject);
        // Only touch AzureLib-identified items, so other mods' stacks never gain NBT from us.
        if (!AzIdentityRegistry.hasIdentity(Mc.item(self))) {
            return;
        }
        AzItemIds.getOrCreate(self);
    }
}
