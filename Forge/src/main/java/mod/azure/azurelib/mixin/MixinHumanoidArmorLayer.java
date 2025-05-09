package mod.azure.azurelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.matrix.MatrixStack;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
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
import net.minecraft.item.ArmorItem;
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
    public abstract A getModelFromSlot(EquipmentSlotType slotIn);

    @ModifyExpressionValue(
        method = "renderArmorPart",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getItemStackFromSlot(Lnet/minecraft/inventory/EquipmentSlotType;)Lnet/minecraft/item/ItemStack;"
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
        method = "renderArmorPart", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/entity/layers/ArmorLayer;renderArmor(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;IZLnet/minecraft/client/renderer/entity/model/BipedModel;FFFLnet/minecraft/util/ResourceLocation;)V"
    ), cancellable = true
    )
    public void azurelib$renderAzurelibModel(
        MatrixStack poseStack, IRenderTypeBuffer bufferSource, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, EquipmentSlotType equipmentSlot, int packedLight, CallbackInfo ci, @Share("item_by_slot") LocalRef<ItemStack> itemBySlotRef
    ) {
        ItemStack stack = itemBySlotRef.get();
        A baseModel = this.getModelFromSlot(equipmentSlot);
        RenderProvider renderProvider = RenderProvider.of(stack);
        @SuppressWarnings("unchecked")
        BipedModel<LivingEntity> humanoidModel = (BipedModel<LivingEntity>) baseModel;
        Model geckolibModel = renderProvider
                                  .getGenericArmorModel(entity, stack, equipmentSlot, humanoidModel);

        if (geckolibModel != null && stack.getItem() instanceof GeoItem) {
            if (geckolibModel instanceof GeoArmorRenderer) {
                GeoArmorRenderer geoArmorRenderer = (GeoArmorRenderer) geckolibModel;
                geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            }

            baseModel.setModelAttributes((A) geckolibModel);

            geckolibModel.render(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
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
