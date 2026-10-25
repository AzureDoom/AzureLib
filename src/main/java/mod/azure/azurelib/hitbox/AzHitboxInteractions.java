package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

import mod.azure.azurelib.platform.mc.Mc;

public final class AzHitboxInteractions {

    private AzHitboxInteractions() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public static AzHitboxHit findLookedAt(PlayerEntity player, Entity target, Vec3d eye, Vec3d look) {
        Vec3d end = AzHitboxMath.along(eye, look, AzHitboxes.attackReach(player));

        return AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable)
            .filter(hit -> hit.hitbox().role() == AzHitboxRole.INTERACT)
            .orElse(null);
    }

    /**
     * Runs at the start of {@code PlayerEntity#interact(Entity)}. On a remote (multiplayer client) world the hit is
     * predicted from the crosshair; everywhere else, including single player, it comes from the claim the client sent.
     */
    public static boolean interact(PlayerEntity player, Entity target) {
        if (!(target instanceof AzHitboxHolder)) {
            return false;
        }

        AzHitboxHit hit = Mc.isClientSide(Mc.world(player))
            ? findLookedAt(player, target, AzHitboxMath.eyePosition(player), AzHitboxMath.look(player))
            : AzHitboxDamageTracker.takeInteract(player, target);

        if (hit == null) {
            return false;
        }

        return ((AzHitboxHolder) target).onHitboxInteract(hit, player);
    }
}
