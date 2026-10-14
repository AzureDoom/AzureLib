package mod.azure.azurelib.render.entity;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;

import mod.azure.azurelib.platform.mc.Mc;
import mod.azure.azurelib.platform.mc.McClient;
import mod.azure.azurelib.render.vertex.GlStateManager;

/**
 * Name tags. Beta only ever draws names above other players (there are no custom name tags), so that is what
 * {@link #shouldShowName(Entity)} reports; renderers can draw any text with
 * {@link #renderNameTag(Entity, String, double, double, double)}.
 */
public final class AzEntityNameRenderUtil {

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }

    public static <T extends Entity> boolean shouldShowName(T entity) {
        return entity instanceof PlayerEntity && entity != McClient.cameraEntity() && Mc.passenger(entity) == null;
    }

    /** The text Beta would draw for this entity: the player name, or {@code null}. MAPPING: PlayerEntity#name. */
    public static String nameOf(Entity entity) {
        return entity instanceof PlayerEntity ? ((PlayerEntity) entity).name : null;
    }

    public static void renderNameTag(Entity entity, double x, double y, double z) {
        String name = nameOf(entity);
        if (name != null) {
            renderNameTag(entity, name, x, y, z);
        }
    }

    /**
     * Draws text above the entity, the way Beta's {@code LivingEntityRenderer} draws player names.
     */
    public static void renderNameTag(Entity entity, String text, double x, double y, double z) {
        double maxDistance = Mc.isSneaking(entity) ? 32.0D : 64.0D;
        if (McClient.distanceToCameraSq(Mc.x(entity), Mc.y(entity), Mc.z(entity)) > maxDistance * maxDistance) {
            return;
        }

        TextRenderer font = McClient.textRenderer();
        float scale = 0.016666668F * 1.6F;

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x, (float) y + Mc.height(entity) + 0.5F, (float) z);
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-McClient.cameraYaw(), 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(McClient.cameraPitch(), 1.0F, 0.0F, 0.0F);
        GL11.glScalef(-scale, -scale, scale);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDepthMask(false);

        if (!Mc.isSneaking(entity)) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
        }

        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.tryBlendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ZERO
        );

        int halfWidth = font.getWidth(text) / 2;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.25F);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3f(-halfWidth - 1, -1.0F, 0.0F);
        GL11.glVertex3f(-halfWidth - 1, 8.0F, 0.0F);
        GL11.glVertex3f(halfWidth + 1, 8.0F, 0.0F);
        GL11.glVertex3f(halfWidth + 1, -1.0F, 0.0F);
        GL11.glEnd();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        if (!Mc.isSneaking(entity)) {
            font.draw(text, -halfWidth, 0, 553648127);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            font.draw(text, -halfWidth, 0, -1);
        } else {
            GL11.glDepthMask(true);
        }

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}
