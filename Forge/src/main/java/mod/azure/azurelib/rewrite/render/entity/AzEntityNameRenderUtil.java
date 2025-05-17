package mod.azure.azurelib.rewrite.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.scoreboard.Team;

import java.util.Objects;

public class AzEntityNameRenderUtil {

    public static <T extends Entity> boolean shouldShowName(EntityRendererManager entityRenderDispatcher, T entity) {
        double nameRenderDistance = entity.isDiscrete() ? 32d : 64d;

        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        if (entityRenderDispatcher.squareDistanceTo(entity) >= nameRenderDistance * nameRenderDistance) {
            return false;
        }

        if (
            entity instanceof MobEntity && (!entity.getAlwaysRenderNameTagForRender() && (!entity.hasCustomName()
                || entity != entityRenderDispatcher.pointedEntity))
        ) {
            return false;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        // TODO: See if we can do this null check better.
        ClientPlayerEntity player = Objects.requireNonNull(minecraft.player);
        boolean visibleToClient = !entity.isInvisibleToPlayer(player);
        Team entityTeam = entity.getTeam();

        if (entityTeam == null) {
            return Minecraft.isGuiEnabled() && entity != minecraft.getRenderViewEntity() && visibleToClient
                && !entity.isBeingRidden();
        }

        Team playerTeam = minecraft.player.getTeam();

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
        }
        return false;
    }

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
