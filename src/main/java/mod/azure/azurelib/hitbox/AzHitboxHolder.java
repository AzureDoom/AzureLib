package mod.azure.azurelib.hitbox;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

public interface AzHitboxHolder {

    AzHitboxSet getHitboxSet();

    default float onHitboxHit(AzHitboxHit hit, DamageSource source, float amount) {
        return amount * hit.hitbox().damageMultiplier();
    }

    default void onHitboxDamaged(AzHitboxHit hit, DamageSource source, float amount) {}

    default boolean onHitboxInteract(AzHitboxHit hit, EntityPlayer player) {
        return false;
    }

    default boolean isHitboxEnabled(AzHitbox hitbox) {
        return true;
    }
}
