package mod.azure.azurelib.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = ArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends BipedModel<T>> {

    @Shadow
    public abstract A getModelFromSlot(EquipmentSlotType slotIn);

    @Inject(method = "renderArmorPart", at = @At(value = "RETURN", target = "Lnet/minecraft/client/renderer/entity/layers/ArmorLayer;isLegSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Z"), cancellable = true)
    public void azurelib$renderGeckoLibModel(MatrixStack poseStack, IRenderTypeBuffer bufferSource, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType equipmentSlot, int packedLight, CallbackInfo ci) {
        final ItemStack stack = entity.getItemStackFromSlot(equipmentSlot);
        if (stack.getItem() instanceof ArmorItem) {
            A baseModel = this.getModelFromSlot(equipmentSlot);
            final Model geckolibModel = RenderProvider.of(stack).getGenericArmorModel(entity, stack, equipmentSlot,
                    (BipedModel<LivingEntity>) baseModel);

            if (geckolibModel != null && stack.getItem() instanceof GeoItem) {
                if (geckolibModel instanceof GeoArmorRenderer) {
                    GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geckolibModel;
                    geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
                }

                baseModel.setModelAttributes((A) geckolibModel);
                geckolibModel.render(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            }
        }
    }
}
