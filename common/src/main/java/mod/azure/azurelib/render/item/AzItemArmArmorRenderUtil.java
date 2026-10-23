package mod.azure.azurelib.render.item;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.Services;
import mod.azure.azurelib.render.AzModelRenderer;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;

public final class AzItemArmArmorRenderUtil {

    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_CACHE = new HashMap<>();

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

        var azRenderer = AzArmorRendererRegistry.getOrNull(chest.getItem());
        if (azRenderer != null) {
            renderAzArmorArm(poseStack, arm, playerModel, chest, azRenderer, packedLight);
        } else if (
            chest.getItem() instanceof ArmorItem armorItem
                && armorItem.getSlot() == EquipmentSlot.CHEST
        ) {
            renderVanillaArmorArm(poseStack, arm, bone, player, armorItem, chest, packedLight, ctx, modelRenderer);
        }
    }

    private static ModelPart armPart(HumanoidModel<?> model, HumanoidArm arm) {
        return arm == HumanoidArm.LEFT ? model.leftArm : model.rightArm;
    }

    private static void renderVanillaArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        AzBone bone,
        LivingEntity player,
        ArmorItem armorItem,
        ItemStack chest,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
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

        // Mirrors HumanoidArmorLayer#renderArmorPiece (1.20.1)
        if (armorItem instanceof DyeableLeatherItem dyeable) {
            int color = dyeable.getColor(chest);
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;
            renderLayer(
                poseStack,
                part,
                player,
                armorItem,
                chest,
                null,
                r,
                g,
                b,
                packedLight,
                ctx,
                bone,
                modelRenderer
            );
            renderLayer(
                poseStack,
                part,
                player,
                armorItem,
                chest,
                "overlay",
                1,
                1,
                1,
                packedLight,
                ctx,
                bone,
                modelRenderer
            );
        } else {
            renderLayer(
                poseStack,
                part,
                player,
                armorItem,
                chest,
                null,
                1,
                1,
                1,
                packedLight,
                ctx,
                bone,
                modelRenderer
            );
        }

        if (chest.hasFoil()) {
            part.render(
                poseStack,
                modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderType.armorEntityGlint()),
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1,
                1,
                1,
                1
            );
        }
    }

    private static void renderLayer(
        PoseStack poseStack,
        ModelPart part,
        LivingEntity player,
        ArmorItem armorItem,
        ItemStack chest,
        @Nullable String type,
        float r,
        float g,
        float b,
        int packedLight,
        AzItemRendererPipelineContext ctx,
        AzBone bone,
        AzModelRenderer<UUID, ItemStack> modelRenderer
    ) {
        var texture = armorTexture(player, armorItem, chest, type);
        if (texture == null) {
            return;
        }

        var vc = modelRenderer.getOrRefreshBufferRenderType(ctx, bone, RenderType.armorCutoutNoCull(texture));
        part.render(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1);
    }

    private static @Nullable ResourceLocation armorTexture(
        LivingEntity entity,
        ArmorItem armorItem,
        ItemStack stack,
        @Nullable String type
    ) {
        var path = Services.clientPlatform()
            .getArmorTexture(
                entity,
                stack,
                EquipmentSlot.CHEST,
                defaultArmorTexturePath(armorItem, type),
                type
            );
        // tryParse returns null for malformed mod paths; computeIfAbsent doesn't cache nulls
        return ARMOR_TEXTURE_CACHE.computeIfAbsent(path, ResourceLocation::tryParse);
    }

    /**
     * Chestplates always use the outer model, so this is always {@code _layer_1}. Handles {@code modid:name} material
     * names the same way Forge's HumanoidArmorLayer#getArmorResource does.
     */
    private static String defaultArmorTexturePath(ArmorItem armorItem, @Nullable String type) {
        var name = armorItem.getMaterial().getName();
        var domain = "minecraft";
        int idx = name.indexOf(':');
        if (idx != -1) {
            domain = name.substring(0, idx);
            name = name.substring(idx + 1);
        }
        return String.format(
            Locale.ROOT,
            "%s:textures/models/armor/%s_layer_1%s.png",
            domain,
            name,
            type == null ? "" : "_" + type
        );
    }

    private static void renderAzArmorArm(
        PoseStack poseStack,
        HumanoidArm arm,
        PlayerModel<?> playerModel,
        ItemStack chest,
        AzArmorRenderer azRenderer,
        int packedLight
    ) {
        var player = Minecraft.getInstance().player;

        azRenderer.prepForRender(player, chest, EquipmentSlot.CHEST, playerModel);

        var basePart = arm == HumanoidArm.LEFT ? playerModel.leftArm : playerModel.rightArm;
        azRenderer.rendererPipeline().context().setVisiblePartOverride(basePart);

        azRenderer.rendererPipeline()
            .armorModel()
            .renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
    }
}
