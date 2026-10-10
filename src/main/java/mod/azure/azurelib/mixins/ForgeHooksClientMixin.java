package mod.azure.azurelib.mixins;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.render.armor.AzArmorModel;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.render.item.AzItemRenderHooks;

/**
 * Forge-side render hooks:
 * <ul>
 * <li>supplies the AzureLib armor model for items registered in {@link AzArmorRendererRegistry} (1.12.2's equivalent of
 * the 1.18 {@code HumanoidArmorLayer} hook)</li>
 * <li>records the camera transform type of the item about to be rendered, since 1.12.2's
 * {@code RenderItem#renderItem(ItemStack, IBakedModel)} doesn't receive it</li>
 * </ul>
 */
@Mixin(value = ForgeHooksClient.class, remap = false)
public abstract class ForgeHooksClientMixin {

    @Inject(method = "getArmorModel", at = @At("RETURN"), cancellable = true)
    private static void azurelib$injectAzureArmors(
        EntityLivingBase entityLiving,
        ItemStack itemStack,
        EntityEquipmentSlot slot,
        ModelBiped defaultModel,
        CallbackInfoReturnable<ModelBiped> cir
    ) {
        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(itemStack.getItem());

        if (renderer != null) {
            AzArmorModel armorModel = renderer.rendererPipeline().armorModel();
            armorModel.prepare(renderer, itemStack, slot);
            cir.setReturnValue(armorModel);
        }
    }

    @Inject(method = "handleCameraTransforms", at = @At("HEAD"))
    private static void azurelib$captureTransformType(
        IBakedModel model,
        ItemCameraTransforms.TransformType cameraTransformType,
        boolean leftHandHackery,
        CallbackInfoReturnable<IBakedModel> cir
    ) {
        AzItemRenderHooks.setTransformType(cameraTransformType);
    }
}
