package mod.azure.azurelib.mixins;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;

@Mixin(Item.class)
public class ItemMixin_EnsureCraftHasID {

    /**
     * Ensures that a unique identifier is assigned to items with registered identities when they are crafted.
     *
     * @param stack  The {@link ItemStack} being crafted. Used to retrieve or create its tag data.
     * @param level  The {@link Level} in which the crafting process occurs. Not directly used in this method.
     * @param player The {@link Player} performing the crafting action. Not directly used in this method.
     * @param ci     The {@link CallbackInfo} provided by the Mixin framework. Used to control execution flow if needed.
     */
    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    public void azureLib$onCraftedByPatch(ItemStack stack, Level level, Player player, CallbackInfo ci) {
        if (!AzIdentityRegistry.hasIdentity(stack.getItem()))
            return;

        var existingTag = stack.getTag();
        if (existingTag != null && existingTag.hasUUID(AzureLib.ITEM_UUID_TAG))
            return;

        stack.getOrCreateTag().putUUID(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
    }
}
