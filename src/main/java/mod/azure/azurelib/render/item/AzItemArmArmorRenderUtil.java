package mod.azure.azurelib.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

import mod.azure.azurelib.model.AzBone;
import mod.azure.azurelib.render.armor.AzArmorRenderer;
import mod.azure.azurelib.render.armor.AzArmorRendererRegistry;
import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.OverlayTexture;
import mod.azure.azurelib.render.vertex.PoseStack;
import mod.azure.azurelib.util.AzEquipmentSlot;
import mod.azure.azurelib.util.client.AzRenderTick;
import mod.azure.azurelib.util.client.RenderUtils;

public final class AzItemArmArmorRenderUtil {

    private static final ResourceLocation ENCHANTED_ITEM_GLINT = new ResourceLocation(
        "textures/misc/enchanted_item_glint.png"
    );

    private static final int CHEST_ARMOR_INDEX = 2;

    private static final int CHEST_ARMOR_TYPE = 1;

    private static final int CHEST_RENDER_PASS = 1;

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
        boolean leftArm,
        AzBone bone,
        ModelBiped playerModel,
        int packedLight
    ) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }

        ItemStack chest = player.getCurrentArmor(CHEST_ARMOR_INDEX);
        if (chest == null || chest.getItem() == null) {
            return;
        }

        AzArmorRenderer azRenderer = AzArmorRendererRegistry.getOrNull(chest.getItem());
        if (azRenderer != null) {
            renderAzArmorArm(poseStack, leftArm, bone, playerModel, chest, azRenderer, packedLight);
            return;
        }

        if (
            chest.getItem() instanceof ItemArmor
                && ((ItemArmor) chest.getItem()).armorType == CHEST_ARMOR_TYPE
        ) {
            renderVanillaArmorArm(bufferSource, poseStack, leftArm, bone, player, (ItemArmor) chest.getItem(), chest);
        }
    }

    private static ModelRenderer armPart(ModelBiped model, boolean leftArm) {
        return leftArm ? model.bipedLeftArm : model.bipedRightArm;
    }

    private static void renderVanillaArmorArm(
        MultiBufferSource bufferSource,
        PoseStack poseStack,
        boolean leftArm,
        AzBone bone,
        EntityLivingBase player,
        ItemArmor armorItem,
        ItemStack chest
    ) {
        TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

        ModelBiped model = ForgeHooksClient.getArmorModel(player, chest, CHEST_RENDER_PASS, outerArmorModel());
        ModelRenderer part = armPart(model, leftArm);

        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        GL11.glPushMatrix();
        RenderUtils.applyPoseToGl(poseStack.last());

        int color = armorItem.getColor(chest);
        textureManager.bindTexture(RenderBiped.getArmorResource(player, chest, CHEST_RENDER_PASS, null));

        if (color != -1) {
            float r = (color >> 16 & 255) / 255.0F;
            float g = (color >> 8 & 255) / 255.0F;
            float b = (color & 255) / 255.0F;

            GL11.glColor4f(r, g, b, 1.0F);
            AzItemArmRenderUtil.renderPart(part, bone);

            textureManager.bindTexture(RenderBiped.getArmorResource(player, chest, CHEST_RENDER_PASS, "overlay"));
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        AzItemArmRenderUtil.renderPart(part, bone);

        if (chest.hasEffect(0)) {
            renderGlint(textureManager, player, part, bone);
        }

        GL11.glPopMatrix();
    }

    private static void renderGlint(
        TextureManager textureManager,
        EntityLivingBase entity,
        ModelRenderer part,
        AzBone bone
    ) {
        float time = entity.ticksExisted + AzRenderTick.partialTicks();

        textureManager.bindTexture(ENCHANTED_ITEM_GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glColor4f(0.5F, 0.5F, 0.5F, 1.0F);
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDepthMask(false);

        for (int i = 0; i < 2; ++i) {
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor4f(0.38F, 0.19F, 0.608F, 1.0F);
            GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GL11.glLoadIdentity();
            GL11.glScalef(0.33333334F, 0.33333334F, 0.33333334F);
            GL11.glRotatef(30.0F - i * 60.0F, 0.0F, 0.0F, 1.0F);
            GL11.glTranslatef(0.0F, time * (0.001F + i * 0.003F) * 20.0F, 0.0F);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            AzItemArmRenderUtil.renderPart(part, bone);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glDepthMask(true);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    private static void renderAzArmorArm(
        PoseStack poseStack,
        boolean leftArm,
        AzBone bone,
        ModelBiped playerModel,
        ItemStack chest,
        AzArmorRenderer azRenderer,
        int packedLight
    ) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ModelRenderer basePart = armPart(playerModel, leftArm);

        float oldX = basePart.rotationPointX, oldY = basePart.rotationPointY, oldZ = basePart.rotationPointZ;
        float oldRotX = basePart.rotateAngleX, oldRotY = basePart.rotateAngleY, oldRotZ = basePart.rotateAngleZ;

        basePart.setRotationPoint(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        basePart.rotateAngleX = 0;
        basePart.rotateAngleY = 0;
        basePart.rotateAngleZ = 0;

        try {
            azRenderer.prepForRender(player, chest, AzEquipmentSlot.CHEST, playerModel);

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
