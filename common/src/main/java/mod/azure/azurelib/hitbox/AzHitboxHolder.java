package mod.azure.azurelib.hitbox;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

public interface AzHitboxHolder {

    AzHitboxSet getHitboxSet();

    default float onHitboxHit(AzHitboxHit hit, DamageSource source, float amount) {
        return amount * hit.hitbox().damageMultiplier();
    }

    default InteractionResult onHitboxInteract(AzHitboxHit hit, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }
}
