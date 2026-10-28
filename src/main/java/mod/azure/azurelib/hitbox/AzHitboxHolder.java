package mod.azure.azurelib.hitbox;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

import javax.annotation.Nullable;

public interface AzHitboxHolder {

    AzHitboxSet getHitboxSet();

    /**
     * Called on the logical server (the client world in single player) at the start of
     * {@code LivingEntity#damage(Entity, int)}, before vanilla's invulnerability checks. Returns the damage to apply.
     * <p>
     * Beta has no damage sources and health is an integer, so {@code source} is the player for a melee hit or the
     * projectile entity itself (arrow, snowball, egg, fireball) for a projectile hit, and the result is rounded.
     */
    default int onHitboxHit(AzHitboxHit hit, @Nullable Entity source, int amount) {
        return Math.max(0, Math.round(amount * hit.hitbox().damageMultiplier()));
    }

    /**
     * Called on the logical server (the client world in single player) at the end of
     * {@code LivingEntity#damage(Entity, int)}, only if vanilla accepted the damage: it returned {@code true} and the
     * entity lost health. Hits rejected by invulnerability frames never reach this, and it runs at most once per hit.
     * <p>
     * {@code source} is the same entity passed to {@link #onHitboxHit}. {@code amount} is the health the entity
     * actually lost, which during invulnerability frames is only the part above the previous hit. Health has already
     * been reduced, so a health of zero or less means the hit killed it.
     */
    default void onHitboxDamaged(AzHitboxHit hit, @Nullable Entity source, int amount) {}

    /**
     * Called on both sides when a player right-clicks an {@link AzHitboxRole#INTERACT} hitbox, before
     * {@code Entity#interact(PlayerEntity)}. Return {@code true} to consume the click.
     */
    default boolean onHitboxInteract(AzHitboxHit hit, PlayerEntity player) {
        return false;
    }
}
