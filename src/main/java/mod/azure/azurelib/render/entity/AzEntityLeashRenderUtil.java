package mod.azure.azurelib.render.entity;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import org.lwjgl.opengl.GL11;

import mod.azure.azurelib.render.vertex.AzBufferSource;
import mod.azure.azurelib.render.vertex.GlStateManager;
import mod.azure.azurelib.render.vertex.MultiBufferSource;
import mod.azure.azurelib.render.vertex.PoseStack;

/**
 * Leash rendering for AzureLib entities. AzureLib's entity renderer does not extend {@code RenderLiving}, so this
 * reproduces 1.7.10's {@code RenderLiving} leash rendering. It must be called with the GL matrix translated to the
 * entity's render position (which is the case inside {@link AzEntityRenderer#doRender}).
 */
public class AzEntityLeashRenderUtil {

    public static <T extends Entity, E extends Entity, M extends EntityLiving> void renderLeash(
        AzEntityRenderer<T> azEntityRenderer,
        M mob,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource bufferSource,
        E leashHolder
    ) {
        if (bufferSource instanceof AzBufferSource) {
            ((AzBufferSource) bufferSource).endBatch();
        }

        double y = -(1.6D - mob.height) * 0.5D;
        double holderYaw = interpolate(leashHolder.prevRotationYaw, leashHolder.rotationYaw, partialTick * 0.5F)
            * 0.01745329238474369D;
        double holderPitch = interpolate(leashHolder.prevRotationPitch, leashHolder.rotationPitch, partialTick * 0.5F)
            * 0.01745329238474369D;
        double cosYaw = Math.cos(holderYaw);
        double sinYaw = Math.sin(holderYaw);
        double sinPitch = Math.sin(holderPitch);

        if (leashHolder instanceof EntityHanging) {
            cosYaw = 0.0D;
            sinYaw = 0.0D;
            sinPitch = -1.0D;
        }

        double cosPitch = Math.cos(holderPitch);
        double holderX = interpolate(leashHolder.prevPosX, leashHolder.posX, partialTick) - cosYaw * 0.7D - sinYaw
            * 0.5D * cosPitch;
        double holderY = interpolate(
            leashHolder.prevPosY + leashHolder.getEyeHeight() * 0.7D,
            leashHolder.posY + leashHolder.getEyeHeight() * 0.7D,
            partialTick
        ) - sinPitch * 0.5D - 0.25D;
        double holderZ = interpolate(leashHolder.prevPosZ, leashHolder.posZ, partialTick) - sinYaw * 0.7D + cosYaw
            * 0.5D * cosPitch;
        double bodyYaw = interpolate(mob.prevRenderYawOffset, mob.renderYawOffset, partialTick) * 0.01745329238474369D
            + Math.PI / 2D;
        double offsetX = Math.cos(bodyYaw) * mob.width * 0.4D;
        double offsetZ = Math.sin(bodyYaw) * mob.width * 0.4D;
        double mobX = interpolate(mob.prevPosX, mob.posX, partialTick) + offsetX;
        double mobY = interpolate(mob.prevPosY, mob.posY, partialTick);
        double mobZ = interpolate(mob.prevPosZ, mob.posZ, partialTick) + offsetZ;
        double x = offsetX;
        double z = offsetZ;
        double dx = (float) (holderX - mobX);
        double dy = (float) (holderY - mobY);
        double dz = (float) (holderZ - mobZ);

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();

        Tessellator tessellator = Tessellator.instance;

        tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
        for (int segment = 0; segment <= 24; ++segment) {
            float[] color = segmentColor(segment);
            float progress = segment / 24.0F;
            double py = y + dy * (progress * progress + progress) * 0.5D + ((24.0F - segment) / 18.0F + 0.125F);
            tessellator.setColorRGBA_F(color[0], color[1], color[2], 1.0F);
            tessellator.addVertex(x + dx * progress, py, z + dz * progress);
            tessellator.setColorRGBA_F(color[0], color[1], color[2], 1.0F);
            tessellator.addVertex(x + dx * progress + 0.025D, py + 0.025D, z + dz * progress);
        }
        tessellator.draw();

        tessellator.startDrawing(GL11.GL_TRIANGLE_STRIP);
        for (int segment = 0; segment <= 24; ++segment) {
            float[] color = segmentColor(segment);
            float progress = segment / 24.0F;
            double py = y + dy * (progress * progress + progress) * 0.5D + ((24.0F - segment) / 18.0F + 0.125F);
            tessellator.setColorRGBA_F(color[0], color[1], color[2], 1.0F);
            tessellator.addVertex(x + dx * progress, py + 0.025D, z + dz * progress);
            tessellator.setColorRGBA_F(color[0], color[1], color[2], 1.0F);
            tessellator.addVertex(x + dx * progress + 0.025D, py, z + dz * progress + 0.025D);
        }
        tessellator.draw();

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.enableCull();
    }

    private static float[] segmentColor(int segment) {
        float red = 0.5F;
        float green = 0.4F;
        float blue = 0.3F;

        if (segment % 2 == 0) {
            red *= 0.7F;
            green *= 0.7F;
            blue *= 0.7F;
        }

        return new float[] { red, green, blue };
    }

    private static double interpolate(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    private AzEntityLeashRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
