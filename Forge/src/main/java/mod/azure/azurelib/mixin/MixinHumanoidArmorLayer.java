package mod.azure.azurelib.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorModel;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = BipedArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends BipedModel<T>> {

    @Inject(method = "renderArmorPiece", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/BipedArmorLayer;usesInnerModel(Lnet/minecraft/inventory/EquipmentSlotType;)Z"), cancellable = true)
    public void azurelib$renderAzModel(MatrixStack poseStack, IRenderTypeBuffer bufferSource, T entity, EquipmentSlotType equipmentSlot, int packedLight, A baseModel, CallbackInfo ci) {
        final ItemStack stack = entity.getItemBySlot(equipmentSlot);
        final Model geoModel = RenderProvider.of(stack).getGenericArmorModel(entity, stack, equipmentSlot,
            (BipedModel<LivingEntity>) baseModel);

        if (geoModel != null && stack.getItem() instanceof GeoItem) {
            if (geoModel instanceof GeoArmorRenderer) {
                GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geoModel;
                geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            }

            baseModel.copyPropertiesTo((A) geoModel);
            geoModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }

        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(stack.getItem());

        if (renderer != null) {
            AzArmorRendererPipeline rendererPipeline = renderer.rendererPipeline();
            AzArmorModel<?> armorModel = rendererPipeline.armorModel();
            @SuppressWarnings("unchecked")
            BipedModel<T> typedHumanoidModel = (BipedModel<T>) armorModel;

            renderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            baseModel.copyPropertiesTo(typedHumanoidModel);
            azurelib$testVisibility((A) typedHumanoidModel, entity, equipmentSlot);
            armorModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }
    }

    @Unique
    private void azurelib$testVisibility(A model, Entity entity, EquipmentSlotType equipmentSlot) {
        if (entity instanceof PlayerEntity && model instanceof PlayerModel<?>) {
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
