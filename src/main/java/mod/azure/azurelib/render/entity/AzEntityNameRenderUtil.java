package mod.azure.azurelib.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

/**
 * Name tag rules and drawing for AzureLib entities, following 1.7.10's {@code RendererLivingEntity} and
 * {@code RenderLiving}.
 */
public class AzEntityNameRenderUtil {

    public static <T extends Entity> boolean shouldShowName(RenderManager renderManager, T entity) {
        if (!(entity instanceof EntityLivingBase)) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.thePlayer == null) {
            return false;
        }

        if (entity instanceof EntityLiving) {
            EntityLiving living = (EntityLiving) entity;
            MovingObjectPosition target = minecraft.objectMouseOver;
            boolean pointedAt = target != null && target.entityHit == entity;

            if (!living.getAlwaysRenderNameTagForRender() && !(living.hasCustomNameTag() && pointedAt)) {
                return false;
            }
        }

        return Minecraft.isGuiEnabled() && entity != renderManager.livingPlayer && !entity.isInvisibleToPlayer(
            minecraft.thePlayer
        ) && entity.riddenByEntity == null;
    }

    /**
     * Draws the entity's name above it. A copy of 1.7.10's {@code Render#func_147906_a}, which is protected.
     */
    public static void renderNameTag(RenderManager renderManager, Entity entity, double x, double y, double z) {
        String text = entity instanceof EntityLiving && ((EntityLiving) entity).hasCustomNameTag()
            ? ((EntityLiving) entity).getCustomNameTag()
            : entity.getCommandSenderName();
        double maxDistance = entity.isSneaking() ? 32.0D : 64.0D;

        if (
            renderManager.livingPlayer == null || entity.getDistanceSqToEntity(renderManager.livingPlayer) > maxDistance
                * maxDistance
        ) {
            return;
        }

        FontRenderer font = renderManager.getFontRenderer();
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + entity.height + 0.5F, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        if (!entity.isSneaking()) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        Tessellator tessellator = Tessellator.instance;
        int halfWidth = font.getStringWidth(text) / 2;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
        tessellator.addVertex(-halfWidth - 1, -1.0D, 0.0D);
        tessellator.addVertex(-halfWidth - 1, 8.0D, 0.0D);
        tessellator.addVertex(halfWidth + 1, 8.0D, 0.0D);
        tessellator.addVertex(halfWidth + 1, -1.0D, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (!entity.isSneaking()) {
            font.drawString(text, -halfWidth, 0, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            font.drawString(text, -halfWidth, 0, -1);
        } else {
            GL11.glDepthMask(true);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
