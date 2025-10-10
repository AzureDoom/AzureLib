package mod.azure.azurelib.render.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Objects;

public class AzEntityNameRenderUtil {

    public static <T extends Entity> boolean shouldShowName(EntityRenderDispatcher entityRenderDispatcher, T entity) {
        var nameRenderDistance = entity.isDiscrete() ? 32d : 64d;

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

        final var minecraft = Minecraft.getInstance();
        // TODO: See if we can do this null check better.
        var player = Objects.requireNonNull(minecraft.player);
        var visibleToClient = !entity.isInvisibleTo(player);
        var entityTeam = entity.getTeam();

        if (entityTeam == null) {
            return Minecraft.renderNames() && entity != minecraft.getCameraEntity() && visibleToClient
                && !entity.isVehicle();
        }

        var playerTeam = minecraft.player.getTeam();

        return switch (entityTeam.getNameTagVisibility()) {
            case ALWAYS -> visibleToClient;
            case NEVER -> false;
            case HIDE_FOR_OTHER_TEAMS -> playerTeam == null
                ? visibleToClient
                : entityTeam.isAlliedTo(
                    playerTeam
                ) && (entityTeam.canSeeFriendlyInvisibles() || visibleToClient);
            case HIDE_FOR_OWN_TEAM ->
                playerTeam == null ? visibleToClient : !entityTeam.isAlliedTo(playerTeam) && visibleToClient;
        };
    }

    private AzEntityNameRenderUtil() {
        throw new UnsupportedOperationException();
    }
}
