package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import mod.azure.azurelib.animation.cache.AzItemIdentity;

@Mixin(value = ClientHooks.class, remap = false)
public abstract class ClientHooksMixin_AzIdentityReequip {

    @WrapOperation(
        method = "shouldCauseReequipAnimation",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/Item;shouldCauseReequipAnimation(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Z"
        ),
        remap = false
    )
    private static boolean azurelib$ignoreIdentityOnlyChanges(
        Item item,
        ItemStack from,
        ItemStack to,
        boolean slotChanged,
        Operation<Boolean> original
    ) {
        boolean reequip = original.call(item, from, to, slotChanged);
        return reequip && (slotChanged || !AzItemIdentity.differsOnlyByIdentity(from, to));
    }
}
