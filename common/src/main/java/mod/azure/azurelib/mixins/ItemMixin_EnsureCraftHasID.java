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
import mod.azure.azurelib.common.animation.cache.AzIdentityRegistry;

@Mixin(Item.class)
public class ItemMixin_EnsureCraftHasID {

    /**
     * Injected method that is triggered when an item is crafted. This method ensures that any crafted item which has an
     * identity registered in the AzIdentityRegistry is assigned a unique identifier (UUID) if it doesn't already have
     * one.
     *
     * @param stack  The {@link ItemStack} instance representing the crafted item.
     * @param level  The {@link Level} where the crafting event occurs.
     * @param player The {@link Player} who performed the crafting action.
     * @param ci     The {@link CallbackInfo} containing the context of the method call.
     */
    @Inject(method = "onCraftedBy", at = @At("HEAD"))
    public void azureLib$onCraftedByPatch(ItemStack stack, Level level, Player player, CallbackInfo ci) {
        if (AzIdentityRegistry.hasIdentity(stack.getItem()) && !stack.has(AzureLib.AZ_ID.get())) {
            stack.set(AzureLib.AZ_ID.get(), UUID.randomUUID());
        }
    }
}
