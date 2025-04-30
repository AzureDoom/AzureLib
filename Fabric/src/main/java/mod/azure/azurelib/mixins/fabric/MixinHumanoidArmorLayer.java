/**
 * This class is a fork of the matching class found in the Geckolib repository.
 * Original source: https://github.com/bernie-g/geckolib
 * Copyright © 2024 Bernie-G.
 * Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.mixins.fabric;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends HumanoidModel<T>> {

    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"), cancellable = true)
    public void azurelib$renderGeckoLibModel(PoseStack poseStack, MultiBufferSource bufferSource, T entity, EquipmentSlot equipmentSlot, int packedLight, A baseModel, CallbackInfo ci) {
        final ItemStack stack = entity.getItemBySlot(equipmentSlot);
        final Model geckolibModel = RenderProvider.of(stack).getGenericArmorModel(entity, stack, equipmentSlot,
                (HumanoidModel<LivingEntity>) baseModel);

        if (geckolibModel != null && stack.getItem() instanceof GeoItem) {
            if (geckolibModel instanceof GeoArmorRenderer) {
                GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geckolibModel;
                geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            }

            baseModel.copyPropertiesTo((A) geckolibModel);
            geckolibModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }
    }
}
