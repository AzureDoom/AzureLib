package mod.azure.azurelib.common.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

import mod.azure.azurelib.common.model.AzBone;
import mod.azure.azurelib.common.platform.Services;
import mod.azure.azurelib.common.render.AzModelRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRenderer;
import mod.azure.azurelib.common.render.armor.AzArmorRendererRegistry;

public final class AzItemArmArmorRenderUtil {

    private static HumanoidModel<LivingEntity> outerArmorModel;

    private AzItemArmArmorRenderUtil() {}

    private static HumanoidModel<LivingEntity> outerArmorModel() {
        if (outerArmorModel == null) {
            outerArmorModel = new HumanoidModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)
            );
        }
        return outerArmorModel;
    }

    public static void renderArmArmor(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        PlayerModel<?> playerModel,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        var chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.isEmpty())
            return;

        var azRenderer = AzArmorRendererRegistry.getOrNull(chest);
        if (azRenderer != null) {
            renderAzArmorArm(poseStack, arm, bone, playerModel, chest, azRenderer, packedLight, ctx, modelRenderer);
        } else if (
            chest.getItem() instanceof ArmorItem armorItem
                && armorItem.getEquipmentSlot() == EquipmentSlot.CHEST
        ) {
            renderVanillaArmorArm(poseStack, arm, bone, armorItem, chest, packedLight, ctx, modelRenderer);
        }
    }

    private static ModelPart armPart(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
    }

    private static void renderVanillaArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        ArmorItem armorItem,
        ItemStack chest,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        var player = Minecraft.getInstance().player;

        var fallback = outerArmorModel();
        var fallbackPart = armPart(fallback, arm);
        fallbackPart.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        fallbackPart.setRotation(0, 0, 0);

        var model = Services.clientPlatform().getArmorModel(player, chest, EquipmentSlot.CHEST, fallback);
        if (model == null) {
            return;
        }

        var part = armPart(model, arm);
        if (model != fallback) {
            part.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
            part.setRotation(0, 0, 0);
        }

        var material = armorItem.getMaterial();
        var layers = material.value().layers();

        for (int i = 0; i < layers.size(); i++) {
            var layer = layers.get(i);

            int color = Services.clientPlatform().getArmorLayerColor(player, chest, layer, i);
            if (color == 0) {
                continue;
            }

            var texture = Services.clientPlatform().getArmorTexture(player, chest, layer, false, EquipmentSlot.CHEST);
            var vc = modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderType.armorCutoutNoCull(texture));
            part.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color);
        }

        var trim = chest.get(DataComponents.TRIM);
        if (trim != null) {
            var atlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
            var sprite = atlas.getSprite(trim.outerTexture(material));
            var vc = sprite.wrap(
                modelRenderer.getOrRefreshBufferRenderType(
                    ctx,
                    bone,
                    Sheets.armorTrimsSheet(trim.pattern().value().decal())
                )
            );
            part.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        }

        if (chest.hasFoil()) {
            part.render(
                poseStack,
                modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderType.armorEntityGlint()),
                packedLight,
                OverlayTexture.NO_OVERLAY
            );
        }
    }

    private static void renderAzArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        PlayerModel<?> playerModel,
        ItemStack chest,
        AzArmorRenderer azRenderer,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        var player = Minecraft.getInstance().player;

        azRenderer.prepForRender(player, chest, EquipmentSlot.CHEST, playerModel);

        var basePart = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
        azRenderer.rendererPipeline().context().setVisiblePartOverride(basePart);

        azRenderer.rendererPipeline()
            .armorModel()
            .renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, -1);
    }
}
