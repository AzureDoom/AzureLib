package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
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

import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = HumanoidArmorLayer.class, priority = 700)
public class NeoMixinHumanoidArmorLayer<T extends LivingEntity, A extends HumanoidModel<T>> {

    @ModifyExpressionValue(
        method = "renderArmorPiece",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private ItemStack azurelib$captureItemBySlot(
        ItemStack original,
        @Share("item_by_slot") LocalRef<ItemStack> itemBySlotRef
    ) {
        itemBySlotRef.set(original);
        return original;
    }

    @Inject(
        method = "renderArmorPiece", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V"
        ), cancellable = true
    )
    public void azurelib$renderAzurelibModel(
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        T entity,
        EquipmentSlot equipmentSlot,
        int packedLight,
        A baseModel,
        CallbackInfo ci,
        @Share("item_by_slot") LocalRef<ItemStack> itemBySlotRef
    ) {
        var stack = itemBySlotRef.get();

        var renderer = AzArmorRendererRegistry.getOrNull(stack.getItem());

        if (renderer != null) {
            var rendererPipeline = renderer.rendererPipeline();
            var armorModel = rendererPipeline.armorModel();
            @SuppressWarnings("unchecked")
            var typedHumanoidModel = (HumanoidModel<T>) armorModel;

            renderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            baseModel.copyPropertiesTo(typedHumanoidModel);
            armorModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }
    }
}
