package mod.azure.azurelib.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.scoreboard.Team;

/**
 * Name tag visibility rules for AzureLib entities, following 1.12.2's {@code RenderLivingBase#canRenderName} and
 * {@code RenderLiving#canRenderName}, plus the 1.18 distance cut-off.
 */
public class AzEntityNameRenderUtil {

    public static <T extends Entity> boolean shouldShowName(RenderManager renderManager, T entity) {
        if (!(entity instanceof EntityLivingBase)) {
            return false;
        }

        Entity viewer = renderManager.renderViewEntity;
        double nameRenderDistance = entity.isSneaking() ? 32d : 64d;

        if (viewer != null && entity.getDistanceSq(viewer) >= nameRenderDistance * nameRenderDistance) {
            return false;
        }

        if (
            entity instanceof EntityLiving && !entity.getAlwaysRenderNameTagForRender() && !(entity.hasCustomName()
                && entity == renderManager.pointedEntity)
        ) {
            return false;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;

        if (player == null) {
            return false;
        }

        boolean visibleToClient = !entity.isInvisibleToPlayer(player);

        if (entity != player) {
            Team entityTeam = entity.getTeam();
            Team playerTeam = player.getTeam();

            if (entityTeam != null) {
                switch (entityTeam.getNameTagVisibility()) {
                    case ALWAYS:
                        return visibleToClient;
                    case NEVER:
                        return false;
                    case HIDE_FOR_OTHER_TEAMS:
                        return playerTeam == null
                            ? visibleToClient
                            : entityTeam.isSameTeam(playerTeam) && (entityTeam.getSeeFriendlyInvisiblesEnabled()
                                || visibleToClient);
                    case HIDE_FOR_OWN_TEAM:
                        return playerTeam == null
                            ? visibleToClient
                            : !entityTeam.isSameTeam(playerTeam) && visibleToClient;
                    default:
                        return true;
                }
            }
        }

        return Minecraft.isGuiEnabled() && entity != renderManager.renderViewEntity && visibleToClient && !entity
            .isBeingRidden();
    }

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
