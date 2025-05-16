package mod.azure.azurelib.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorModel;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRenderer;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererPipeline;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.ArmorLayer;
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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = ArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends BipedModel<T>> {

	@Shadow
	public A getModelFromSlot(EquipmentSlotType slotIn) {
		return null;
	}

	@Inject(method = "renderArmorPart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/ArmorLayer;isLegSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Z"), cancellable = true)
    public void azurelib$renderAzModel(MatrixStack poseStack, IRenderTypeBuffer p_229129_2_, T entity, float p_229129_4_, float p_229129_5_, float p_229129_6_, float p_229129_7_, float p_229129_8_, float p_229129_9_, EquipmentSlotType equipmentSlot, int packedLight, CallbackInfo ci) {
        A baseModel = this.getModelFromSlot(equipmentSlot);
        final ItemStack stack = entity.getItemStackFromSlot(equipmentSlot);
        final Model geoModel = RenderProvider.of(stack).getGenericArmorModel(entity, stack, equipmentSlot,
            (BipedModel<LivingEntity>) baseModel);

        if (geoModel != null && stack.getItem() instanceof GeoItem) {
            if (geoModel instanceof GeoArmorRenderer) {
                GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geoModel;
                geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            }

            baseModel.setModelAttributes((A) geoModel);
            geoModel.render(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }

        AzArmorRenderer renderer = AzArmorRendererRegistry.getOrNull(stack.getItem());

        if (renderer != null) {
            AzArmorRendererPipeline rendererPipeline = renderer.rendererPipeline();
            AzArmorModel<?> armorModel = rendererPipeline.armorModel();
            @SuppressWarnings("unchecked")
            BipedModel<T> typedHumanoidModel = (BipedModel<T>) armorModel;

            renderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            baseModel.setModelAttributes(typedHumanoidModel);
            azurelib$testVisibility((A) typedHumanoidModel, entity, equipmentSlot);
            armorModel.render(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
            ci.cancel();
        }
    }

    @Unique
    private void azurelib$testVisibility(A model, Entity entity, EquipmentSlotType equipmentSlot) {
        if (entity instanceof PlayerEntity && model instanceof PlayerModel<?>) {
            PlayerModel<?> playerModel = (PlayerModel<?>) model;
            switch (equipmentSlot) {
                case HEAD: {
                    playerModel.bipedHeadwear.showModel = false;
                    playerModel.bipedDeadmau5Head.showModel = false;
                    break;
                }
                case CHEST: {
                    playerModel.bipedBodyWear.showModel = false;
                    playerModel.bipedRightArmwear.showModel = false;
                    playerModel.bipedLeftArmwear.showModel = false;
                    break;
                }
                case LEGS: {
                    playerModel.bipedLeftLegwear.showModel = false;
                    playerModel.bipedRightLegwear.showModel = false;
                    break;
                }
            }
        }
    }
}
