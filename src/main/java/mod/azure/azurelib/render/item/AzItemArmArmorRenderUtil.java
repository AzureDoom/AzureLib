package mod.azure.azurelib.render.item;

import net.minecraft.client.render.model.ModelPart;
import net.minecraft.client.render.model.entity.HumanoidModel;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.render.texture.TextureManager;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.ResourceLocation;
import mod.azure.azurelib.util.client.RenderUtils;

public final class AzItemArmArmorRenderUtil {

    private static final int CHEST_ARMOR_INDEX = 2;

    private static final int CHEST_ARMOR_TYPE = 1;

    private static final String[] ARMOR_PREFIXES = { "cloth", "chain", "iron", "diamond", "gold" };

    private static final ResourceLocation[] CHEST_TEXTURES = new ResourceLocation[ARMOR_PREFIXES.length];

    private static final HumanoidModel OUTER_ARMOR_MODEL = new HumanoidModel(1.0F);

    private AzItemArmArmorRenderUtil() {}

    public static void renderArmArmor(
            MultiBufferSource bufferSource,
            PoseStack poseStack,
            boolean leftArm,
            AzBone bone,
            HumanoidModel playerModel,
            int packedLight
    ) {
        PlayerEntity player = McClient.player();

        if (player == null) {
            return;
        }

        ItemStack chest = Mc.armor(player, CHEST_ARMOR_INDEX);
        Item item = Mc.item(chest);

        if (item == null) {
            return;
        }

        AzArmorRenderer azRenderer = AzArmorRendererRegistry.getOrNull(item);

        if (azRenderer != null) {
            renderAzArmorArm(player, poseStack, leftArm, bone, playerModel, chest, azRenderer, packedLight);
            return;
        }

        if (Mc.armorType(item) == CHEST_ARMOR_TYPE) {
            renderVanillaArmorArm(bufferSource, poseStack, leftArm, bone, item);
        }
    }

    private static ModelPart armPart(HumanoidModel model, boolean leftArm) {
        return leftArm ? model.leftArm : model.rightArm;
    }

    private static void renderVanillaArmorArm(
            MultiBufferSource bufferSource,
            PoseStack poseStack,
            boolean leftArm,
            AzBone bone,
            Item item
    ) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());

        TextureManager.getInstance().bindTexture(chestTexture(item));
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        AzItemArmRenderUtil.renderPart(armPart(OUTER_ARMOR_MODEL, leftArm), bone);

        GlStateManager.popMatrix();
    }

    private static ResourceLocation chestTexture(Item item) {
        int index = Mc.armorTextureIndex(item);

        if (index < 0 || index >= ARMOR_PREFIXES.length) {
            index = 2;
        }

        ResourceLocation texture = CHEST_TEXTURES[index];

        if (texture == null) {
            texture = new ResourceLocation(
                    ResourceLocation.DEFAULT_NAMESPACE,
                    "armor/" + ARMOR_PREFIXES[index] + "_1.png"
            );
            CHEST_TEXTURES[index] = texture;
        }

        return texture;
    }

    private static void renderAzArmorArm(
            PlayerEntity player,
            PoseStack poseStack,
            boolean leftArm,
            AzBone bone,
            HumanoidModel playerModel,
            ItemStack chest,
            AzArmorRenderer azRenderer,
            int packedLight
    ) {
        ModelPart basePart = armPart(playerModel, leftArm);

        float oldX = basePart.x, oldY = basePart.y, oldZ = basePart.z;
        float oldRotX = basePart.rotationX, oldRotY = basePart.rotationY, oldRotZ = basePart.rotationZ;

        McClient.setPivot(basePart, bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        basePart.rotationX = 0;
        basePart.rotationY = 0;
        basePart.rotationZ = 0;

        try {
            azRenderer.prepForRender(player, chest, AzEquipmentSlot.CHEST, playerModel);
            azRenderer.rendererPipeline().context().setVisiblePartOverride(basePart);

            azRenderer.rendererPipeline()
                    .armorModel()
                    .renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        } finally {
            azRenderer.rendererPipeline().context().setVisiblePartOverride(null);

            McClient.setPivot(basePart, oldX, oldY, oldZ);
            basePart.rotationX = oldRotX;
            basePart.rotationY = oldRotY;
            basePart.rotationZ = oldRotZ;
        }
    }
}