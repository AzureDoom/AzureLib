package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.client.RenderUtils;

public final class AzItemArmArmorRenderUtil {

    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png"
    );

    private static final Map<String, ResourceLocation> ARMOR_TEXTURE_CACHE = new HashMap<>();

    @Nullable
    private static ModelBiped outerArmorModel;

    private AzItemArmArmorRenderUtil() {}

    private static ModelBiped outerArmorModel() {
        if (outerArmorModel == null) {
            outerArmorModel = new ModelBiped(1.0F);
        }
        return outerArmorModel;
    }

    public static void renderArmArmor(
        MultiBufferSource bufferSource,
        PoseStack poseStack,
        EnumHandSide arm,
        AzBone bone,
        ModelPlayer playerModel,
        int packedLight
    ) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;
        if (player == null) {
            return;
        }

        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty()) {
            return;
        }

        AzArmorRenderer azRenderer = AzArmorRendererRegistry.getOrNull(chest.getItem());
        if (azRenderer != null) {
            renderAzArmorArm(poseStack, arm, bone, playerModel, chest, azRenderer, packedLight);
            return;
        }

        if (
            chest.getItem() instanceof ItemArmor
                && ((ItemArmor) chest.getItem()).armorType == EntityEquipmentSlot.CHEST
        ) {
            renderVanillaArmorArm(bufferSource, poseStack, arm, bone, player, (ItemArmor) chest.getItem(), chest);
        }
    }

    private static ModelRenderer armPart(ModelBiped model, EnumHandSide arm) {
        return arm == EnumHandSide.LEFT ? model.bipedLeftArm : model.bipedRightArm;
    }

    private static void renderVanillaArmorArm(
        MultiBufferSource bufferSource,
        PoseStack poseStack,
        EnumHandSide arm,
        AzBone bone,
        EntityLivingBase player,
        ItemArmor armorItem,
        ItemStack chest
    ) {
        Minecraft mc = Minecraft.getMinecraft();
        TextureManager textureManager = mc.getTextureManager();

        ModelBiped model = ForgeHooksClient.getArmorModel(player, chest, EntityEquipmentSlot.CHEST, outerArmorModel());
        ModelRenderer part = armPart(model, arm);

        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        GlStateManager.pushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());

        if (armorItem.hasOverlay(chest)) {
            int color = armorItem.getColor(chest);
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;

            textureManager.bindTexture(armorTexture(player, chest, armorItem, null));
            GlStateManager.color(r, g, b, 1.0F);
            AzItemArmRenderUtil.renderPart(part, bone);

            textureManager.bindTexture(armorTexture(player, chest, armorItem, "overlay"));
        } else {
            textureManager.bindTexture(armorTexture(player, chest, armorItem, null));
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        AzItemArmRenderUtil.renderPart(part, bone);

        if (chest.hasEffect()) {
            renderGlint(mc, player, part, bone);
        }

        GlStateManager.popMatrix();
    }

    private static ResourceLocation armorTexture(
        EntityLivingBase entity,
        ItemStack stack,
        ItemArmor armorItem,
        @Nullable String type
    ) {
        String texture = armorItem.getArmorMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }

        String path = String.format(
            Locale.ROOT,
            "%s:textures/models/armor/%s_layer_1%s.png",
            domain,
            texture,
            type == null ? "" : "_" + type
        );
        path = ForgeHooksClient.getArmorTexture(entity, stack, path, EntityEquipmentSlot.CHEST, type);

        ResourceLocation location = ARMOR_TEXTURE_CACHE.get(path);
        if (location == null) {
            location = new ResourceLocation(path);
            ARMOR_TEXTURE_CACHE.put(path, location);
        }
        return location;
    }

    private static void renderGlint(Minecraft mc, EntityLivingBase entity, ModelRenderer part, AzBone bone) {
        float time = entity.ticksExisted + mc.getRenderPartialTicks();

        mc.getTextureManager().bindTexture(ENCHANTED_ITEM_GLINT);
        mc.entityRenderer.setupFogColor(true);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(GL11.GL_EQUAL);
        GlStateManager.depthMask(false);
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

        for (int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, time * (0.001F + i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            AzItemArmRenderUtil.renderPart(part, bone);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        }

        GlStateManager.matrixMode(GL11.GL_TEXTURE);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.entityRenderer.setupFogColor(false);
    }

    private static void renderAzArmorArm(
        PoseStack poseStack,
        EnumHandSide arm,
        AzBone bone,
        ModelPlayer playerModel,
        ItemStack chest,
        AzArmorRenderer azRenderer,
        int packedLight
    ) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        ModelRenderer basePart = armPart(playerModel, arm);

        float oldX = basePart.rotationPointX, oldY = basePart.rotationPointY, oldZ = basePart.rotationPointZ;
        float oldRotX = basePart.rotateAngleX, oldRotY = basePart.rotateAngleY, oldRotZ = basePart.rotateAngleZ;

        basePart.setRotationPoint(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        basePart.rotateAngleX = 0;
        basePart.rotateAngleY = 0;
        basePart.rotateAngleZ = 0;

        try {
            azRenderer.prepForRender(player, chest, EntityEquipmentSlot.CHEST, playerModel);

            azRenderer.rendererPipeline().context().setVisiblePartOverride(basePart);

            azRenderer.rendererPipeline()
                .armorModel()
                .renderToBuffer(poseStack, null, packedLight, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        } finally {
            basePart.setRotationPoint(oldX, oldY, oldZ);
            basePart.rotateAngleX = oldRotX;
            basePart.rotateAngleY = oldRotY;
            basePart.rotateAngleZ = oldRotZ;
        }
    }
}
