/**
 * This class is a fork of the matching class found in the Geckolib repository. Original source:
 * https://github.com/bernie-g/geckolib Copyright © 2024 Bernie-G. Licensed under the MIT License.
 * https://github.com/bernie-g/geckolib/blob/main/LICENSE
 */
package mod.azure.azurelib.common.internal.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.common.api.client.renderer.GeoArmorRenderer;
import mod.azure.azurelib.common.api.common.animatable.GeoItem;
import mod.azure.azurelib.common.internal.client.RenderProvider;
import mod.azure.azurelib.rewrite.render.armor.AzArmorRendererRegistry;

@Mixin(HumanoidArmorLayer.class)
public abstract class MixinHumanoidArmorLayer<T extends LivingEntity, A extends HumanoidModel<T>> {

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
            target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;usesInnerModel(Lnet/minecraft/world/entity/EquipmentSlot;)Z"
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
        var renderProvider = RenderProvider.of(stack);
        @SuppressWarnings("unchecked")
        var humanoidModel = (HumanoidModel<LivingEntity>) baseModel;
        var geckolibModel = renderProvider
            .getGenericArmorModel(entity, stack, equipmentSlot, humanoidModel);
        var i2 = stack.is(
            ItemTags.DYEABLE
        ) ? FastColor.ARGB32.opaque(DyedItemColor.getOrDefault(stack, -6265536)) : -1;

        if (geckolibModel != null && stack.getItem() instanceof GeoItem) {
            if (geckolibModel instanceof GeoArmorRenderer<?> geoArmorRenderer) {
                geoArmorRenderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            }

            baseModel.copyPropertiesTo((A) geckolibModel);

            geckolibModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, i2);
            ci.cancel();
        }

        var renderer = AzArmorRendererRegistry.getOrNull(stack);

        if (renderer != null) {
            var rendererPipeline = renderer.rendererPipeline();
            var armorModel = rendererPipeline.armorModel();
            @SuppressWarnings("unchecked")
            var typedHumanoidModel = (HumanoidModel<T>) armorModel;

            renderer.prepForRender(entity, stack, equipmentSlot, baseModel);
            baseModel.copyPropertiesTo(typedHumanoidModel);
            azurelib$testVisibility((A) typedHumanoidModel, entity, equipmentSlot);
            armorModel.renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, i2);
            ci.cancel();
        }
    }

    @Unique
    private void azurelib$testVisibility(A model, @Nullable Entity entity, EquipmentSlot equipmentSlot) {
        if (entity instanceof Player && model instanceof PlayerModel<?> playerModel) {
            switch (equipmentSlot) {
                case HEAD -> {
                    playerModel.hat.visible = false;
                    playerModel.ear.visible = false;
                }
                case CHEST -> {
                    playerModel.jacket.visible = false;
                    playerModel.rightSleeve.visible = false;
                    playerModel.leftSleeve.visible = false;
                }
                case LEGS -> {
                    playerModel.leftPants.visible = false;
                    playerModel.rightPants.visible = false;
                }
            }
        }
    }
}
