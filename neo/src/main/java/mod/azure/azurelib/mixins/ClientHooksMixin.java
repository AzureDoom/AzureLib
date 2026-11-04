package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.animation.cache.AzItemIdentity;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

@Mixin(ForgeHooksClient.class)
public class ClientHooksMixin {

    @Inject(method = "getArmorModel", at = @At("RETURN"), remap = false, cancellable = true)
    private static void injectAzureArmors(
        LivingEntity entityLiving,
        ItemStack itemStack,
        EquipmentSlot slot,
        HumanoidModel<?> _default,
        CallbackInfoReturnable<Model> cir
    ) {
        var renderer = AzArmorRendererRegistry.getOrNull(itemStack.getItem());

        if (renderer != null) {
            var rendererPipeline = renderer.rendererPipeline();
            var armorModel = rendererPipeline.armorModel();
            cir.setReturnValue(armorModel);
        }
    }

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
