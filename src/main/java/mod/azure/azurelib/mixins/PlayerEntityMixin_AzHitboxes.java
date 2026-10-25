package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxInteractions;

/**
 * Runs on every side (server, single player client, and the multiplayer client's local prediction).
 * <ul>
 * <li>{@code attack} is bracketed so the hurt it causes can be matched to the attacking player's melee claim; Beta's
 * {@code damage(Entity, int)} cannot tell a punch from that player's arrow.</li>
 * <li>{@code interact} replaces 1.7.10's {@code EntityInteractEvent} listener: an interaction hitbox may consume the
 * click before {@code Entity#interact(PlayerEntity)} and item use.</li>
 * </ul>
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_AzHitboxes {

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void azurelib$beginMelee(Entity target, CallbackInfo ci) {
        AzHitboxDamageTracker.beginMelee((PlayerEntity) (Object) this, target);
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("RETURN"))
    private void azurelib$endMelee(Entity target, CallbackInfo ci) {
        AzHitboxDamageTracker.endMelee();
    }

    @Inject(method = "interact(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void azurelib$interactHitbox(Entity target, CallbackInfo ci) {
        if (AzHitboxInteractions.interact((PlayerEntity) (Object) this, target)) {
            ci.cancel();
        }
    }
}
