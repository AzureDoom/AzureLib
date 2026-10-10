package mod.azure.azurelib.mixins;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.render.armor.AzArmorModel;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.util.AzEquipmentSlot;

/**
 * Supplies the AzureLib armor model for items registered in {@link AzArmorRendererRegistry}. 1.7.10's armor render
 * passes ask {@code ForgeHooksClient#getArmorModel} for the model of each worn piece; {@code slot} there is the armor
 * index (0 helmet .. 3 boots).
 */
@Mixin(value = ForgeHooksClient.class, remap = false)
public abstract class ForgeHooksClientMixin {

    @Inject(method = "getArmorModel", at = @At("RETURN"), cancellable = true)
    private static void azurelib$injectAzureArmors(
        EntityLivingBase entityLiving,
        ItemStack itemStack,
        int slot,
        ModelBiped defaultModel,
        CallbackInfoReturnable<ModelBiped> cir
    ) {
        if (itemStack == null) {
            return;
        }

        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(itemStack.getItem());

        if (renderer != null) {
            AzArmorModel armorModel = renderer.rendererPipeline().armorModel();
            armorModel.prepare(renderer, itemStack, AzEquipmentSlot.fromArmorType(slot));
            cir.setReturnValue(armorModel);
        }
    }
}
