package mod.azure.azurelib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.matrix.MatrixStack;
import mod.azure.azurelib.animatable.GeoItem;
import mod.azure.azurelib.animatable.client.RenderProvider;
import mod.azure.azurelib.renderer.GeoArmorRenderer;
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

/**
 * Render hook for injecting AzureLib's armor rendering functionalities
 */
@Mixin(value = BipedArmorLayer.class, priority = 700)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends BipedModel<T>> {

    @ModifyExpressionValue(
        method = "renderArmorPiece",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/LivingEntity;getItemBySlot(Lnet/minecraft/inventory/EquipmentSlotType;)Lnet/minecraft/item/ItemStack;"
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
        target = "Lnet/minecraft/client/renderer/entity/layers/BipedArmorLayer;renderModel(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;IZLnet/minecraft/client/renderer/entity/model/BipedModel;FFFLnet/minecraft/util/ResourceLocation;)V"
    ), cancellable = true
    )
    public void azurelib$renderAzurelibModel(
        MatrixStack poseStack,
        IRenderTypeBuffer bufferSource,
        T entity,
        EquipmentSlotType equipmentSlot,
        int packedLight,
        A baseModel,
        CallbackInfo ci,
        @Share("item_by_slot") LocalRef<ItemStack> itemBySlotRef
    ) {
        ItemStack stack = itemBySlotRef.get();
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

            baseModel.copyPropertiesTo((A) geckolibModel);

            geckolibModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
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
