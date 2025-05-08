package mod.azure.azurelib.mixin;

import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooksClient.class)
public class ClientHooksMixin {

    @Inject(method = "getArmorModel", at = @At("RETURN"), remap = false, cancellable = true)
    private static void injectAzureArmors(
        LivingEntity entityLiving,
        ItemStack itemStack,
        EquipmentSlotType slot,
        BipedModel<?> _default,
        CallbackInfoReturnable<Model> cir
    ) {
        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(itemStack.getItem());

        if (renderer != null) {
            AzArmorRendererPipeline rendererPipeline = renderer.rendererPipeline();
            BipedModel<?> armorModel = rendererPipeline.armorModel();
            cir.setReturnValue(armorModel);
        }
    }
}
