package mod.azure.azurelib.common.hitbox;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class AzHitboxInteractions {

    private AzHitboxInteractions() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable AzHitboxHit findLookedAt(Player player, Entity target) {
        var eye = player.getEyePosition();
        var end = eye.add(player.getViewVector(1f).scale(player.entityInteractionRange()));

        return AzHitboxes.raycast(target, eye, end, AzHitbox::isPickable)
            .filter(hit -> hit.hitbox().role() == AzHitboxRole.INTERACT)
            .orElse(null);
    }

    public static InteractionResult interact(Player player, Entity target, InteractionHand hand) {
        if (!(target instanceof AzHitboxHolder holder) || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        var hit = player instanceof ServerPlayer serverPlayer
            ? AzHitboxDamageTracker.takeInteract(serverPlayer, target)
            : findLookedAt(player, target);

        if (hit == null) {
            return InteractionResult.PASS;
        }

        return holder.onHitboxInteract(hit, player, hand);
    }
}
