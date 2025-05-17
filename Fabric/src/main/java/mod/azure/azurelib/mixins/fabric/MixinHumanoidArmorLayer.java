/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.AbstractArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = AbstractArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends HumanoidModel<T>> {

    @Shadow
    public A getArmorModel(EquipmentSlot slotIn) {
        return null;
    }

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
            target = "Lnet/minecraft/client/renderer/entity/layers/AbstractArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"
        ), cancellable = true
    )
    public void azurelib$renderAzurelibModel(
        PoseStack poseStack,
        MultiBufferSource multiBufferSource,
        T entity,
        float f,
        float g,
        float h,
        float i,
        float j,
        float k,
        EquipmentSlot equipmentSlot,
        int packedLight,
        CallbackInfo ci,
        @Share("item_by_slot") LocalRef<ItemStack> itemBySlotRef
    ) {
        ItemStack stack = itemBySlotRef.get();

        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(stack.getItem());

        if (renderer != null) {
            AzArmorRendererPipeline rendererPipeline = renderer.rendererPipeline();
            Model armorModel = rendererPipeline.armorModel();
            A baseModel = this.getArmorModel(equipmentSlot);
            @SuppressWarnings("unchecked")
            HumanoidModel<T> typedHumanoidModel = (HumanoidModel<T>) armorModel;

            renderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            baseModel.copyPropertiesTo(typedHumanoidModel);
            azurelib$testVisibility((A) typedHumanoidModel, entity, equipmentSlot);
            armorModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }
    }

    @Unique
    private void azurelib$testVisibility(A model, @Nullable Entity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player && model instanceof PlayerModel<?>) {
            PlayerModel<?> playerModel = (PlayerModel<?>) model;
            switch (equipmentSlot) {
                case HEAD: {
                    playerModel.hat.visible = false;
                    playerModel.ear.visible = false;
                    break;
                }
                case CHEST: {
                    playerModel.jacket.visible = false;
                    playerModel.rightSleeve.visible = false;
                    playerModel.leftSleeve.visible = false;
                    break;
                }
                case LEGS: {
                    playerModel.leftPants.visible = false;
                    playerModel.rightPants.visible = false;
                    break;
                }
            }
        }
    }
}
