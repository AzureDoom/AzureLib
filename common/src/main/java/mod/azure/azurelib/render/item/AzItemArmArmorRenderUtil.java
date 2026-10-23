package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.palette.PalettedTextureManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

/**
 * Renders the worn chestplate's sleeve over first-person item arms, for both vanilla equipment-asset armor and AzureLib
 * armor.
 */
public final class AzItemArmArmorRenderUtil {

    private static @Nullable HumanoidModel<HumanoidRenderState> outerArmorModel;

    private static @Nullable EquipmentAssetManager equipmentAssets;

    private AzItemArmArmorRenderUtil() {}

    public static void setEquipmentAssets(EquipmentAssetManager assets) {
        equipmentAssets = assets;
        outerArmorModel = null;
    }

    private static HumanoidModel<HumanoidRenderState> outerArmorModel() {
        if (outerArmorModel == null) {
            outerArmorModel = new HumanoidModel<>(
                Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_ARMOR.chest())
            );
        }
        return outerArmorModel;
    }

    public static void renderArmArmor(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        PlayerModel playerModel,
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
            renderAzArmorArm(poseStack, arm, playerModel, chest, azRenderer, packedLight, ctx);
            return;
        }

        var equippable = chest.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.slot() != EquipmentSlot.CHEST)
            return;

        equippable.assetId()
            .ifPresent(
                assetId -> renderEquipmentArmorArm(
                    poseStack,
                    arm,
                    bone,
                    chest,
                    assetId,
                    packedLight,
                    ctx,
                    modelRenderer
                )
            );
    }

    private static ModelPart armPart(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
    }

    private static void renderEquipmentArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        ItemStack chest,
        ResourceKey<EquipmentAsset> assetId,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        var assets = equipmentAssets;
        if (assets == null)
            return;

        var layerType = EquipmentClientInfo.LayerType.HUMANOID;
        var info = assets.get(assetId);
        var layers = info.getLayers(layerType);
        if (layers.isEmpty())
            return;

        var fallback = outerArmorModel();
        var fallbackPart = armPart(fallback, arm);
        fallbackPart.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        fallbackPart.setRotation(0, 0, 0);

        var model = Services.clientPlatform().getArmorModel(chest, layerType, fallback);
        if (model == null)
            return;

        var part = armPart(model, arm);
        if (model != fallback) {
            part.setPos(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
            part.setRotation(0, 0, 0);
        }

        var dyedColor = chest.get(DataComponents.DYED_COLOR);
        int dyeColor = dyedColor != null ? dyedColor.rgb() : 0;

        for (int i = 0; i < layers.size(); i++) {
            var layer = layers.get(i);

            int color = Services.clientPlatform()
                .getArmorLayerColor(chest, layer, i, vanillaLayerColor(layer, dyeColor));
            if (color == 0)
                continue;

            var texture = Services.clientPlatform()
                .getArmorTexture(chest, layerType, layer, layer.getTextureLocation(layerType));
            var vc = modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderTypes.armorCutoutNoCull(texture));
            part.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, color);
        }

        var trim = chest.get(DataComponents.TRIM);
        if (trim != null) {
            var handle = resolveTrimTexture(info, trim, layerType);
            var renderType = RenderTypes.armorTrim(handle.textureLocation(), trim.pattern().value().decal());
            var vc = handle.wrap(modelRenderer.getOrRefreshBufferRenderType(ctx, bone, renderType));
            part.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
        }

        if (chest.hasFoil()) {
            part.render(
                poseStack,
                modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderTypes.trimmedArmorGlint()),
                packedLight,
                OverlayTexture.NO_OVERLAY
            );
        }
    }

    private static int vanillaLayerColor(EquipmentClientInfo.Layer layer, int dyeColor) {
        var dyeable = layer.dyeable();
        if (dyeable.isEmpty())
            return -1;

        int undyed = dyeable.get().colorWhenUndyed().map(ARGB::opaque).orElse(0);
        return dyeColor != 0 ? ARGB.opaque(dyeColor) : undyed;
    }

    private static PalettedTextureManager.Handle resolveTrimTexture(
        EquipmentClientInfo info,
        ArmorTrim trim,
        EquipmentClientInfo.LayerType layerType
    ) {
        Identifier texture = trim.pattern().value().assetId();
        Identifier palette = trim.material().value().paletteId();

        for (var override : info.trimOverrides()) {
            if (override.predicate().matches(trim)) {
                texture = override.textureId().orElse(texture);
                palette = override.paletteId().orElse(palette);
                break;
            }
        }

        var baseTexture = texture.withPath(p -> layerType.trimAssetPrefix() + "/" + p);

        return Minecraft.getInstance().getPalettedTextureManager().getOrPrepare(baseTexture, palette);
    }

    private static void renderAzArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        PlayerModel playerModel,
        ItemStack chest,
        AzArmorRenderer azRenderer,
        int packedLight,
        AzItemRendererPipelineContext ctx
    ) {
        var player = Minecraft.getInstance().player;
        var basePart = armPart(playerModel, arm);

        azRenderer.renderForBone(
            poseStack,
            ctx.multiBufferSource(),
            player,
            chest,
            EquipmentSlot.CHEST,
            playerModel,
            basePart,
            packedLight
        );
    }
}
