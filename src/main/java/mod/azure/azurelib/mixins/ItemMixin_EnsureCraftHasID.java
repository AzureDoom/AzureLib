package mod.azure.azurelib.mixins;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.animation.cache.AzIdentityRegistry;
import mod.azure.azurelib.util.AzureLibUtil;

@Mixin(Item.class)
public class ItemMixin_EnsureCraftHasID {

    /**
     * Ensures that a unique identifier is assigned to items with registered identities when they are crafted.
     */
    @Inject(method = "onCreated", at = @At("HEAD"))
    public void azureLib$onCraftByPatch(ItemStack stack, World world, EntityPlayer player, CallbackInfo ci) {
        if (stack.isEmpty() || !AzIdentityRegistry.hasIdentity(stack.getItem()))
            return;

        NBTTagCompound existingTag = stack.getTagCompound();

        if (existingTag != null && existingTag.hasUniqueId(AzureLib.ITEM_UUID_TAG))
            return;

        AzureLibUtil.getOrCreateTag(stack).setUniqueId(AzureLib.ITEM_UUID_TAG, UUID.randomUUID());
    }
}
