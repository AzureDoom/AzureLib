package mod.azure.azurelib.rewrite.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.Team;

import java.util.Objects;

public class AzEntityNameRenderUtil {

    public static <T extends Entity> boolean shouldShowName(EntityRenderDispatcher entityRenderDispatcher, T entity) {
        double nameRenderDistance = entity.isDiscrete() ? 32d : 64d;

        if (!(entity instanceof LivingEntity)) {
            return false;
        }

        if (entityRenderDispatcher.distanceToSqr(entity) >= nameRenderDistance * nameRenderDistance) {
            return false;
        }

        if (
            entity instanceof Mob && (!entity.shouldShowName() && (!entity.hasCustomName()
                || entity != entityRenderDispatcher.crosshairPickEntity))
        ) {
            return false;
        }

        final Minecraft minecraft = Minecraft.getInstance();
        // TODO: See if we can do this null check better.
        LocalPlayer player = Objects.requireNonNull(minecraft.player);
        boolean visibleToClient = !entity.isInvisibleTo(player);
        Team entityTeam = entity.getTeam();

        if (entityTeam == null) {
            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && visibleToClient
                && !entity.isVehicle();
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
                           : entityTeam.isAlliedTo(playerTeam) && (entityTeam.canSeeFriendlyInvisibles() || visibleToClient);
            case HIDE_FOR_OWN_TEAM:
                return playerTeam == null
                           ? visibleToClient
                           : !entityTeam.isAlliedTo(playerTeam) && visibleToClient;
        }
        return false;
    }

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
