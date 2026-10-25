package mod.azure.azurelib.mixins.client;

import net.minecraft.client.InteractionManager;
import net.minecraft.client.MultiplayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.client.AzHitboxClientHooks;

@Mixin({ InteractionManager.class, MultiplayerInteractionManager.class })
public abstract class InteractionManagerMixin_AzHitboxClaims {

    @Inject(method = "attackEntity", at = @At("HEAD"), require = 1)
    private void azurelib$sendHitboxClaim(PlayerEntity player, Entity target, CallbackInfo ci) {
        AzHitboxClientHooks.onAttack(player, target);
    }

    @Inject(method = "interactEntity", at = @At("HEAD"), require = 1)
    private void azurelib$sendInteractClaim(PlayerEntity player, Entity target, CallbackInfo ci) {
        AzHitboxClientHooks.onInteract(player, target);
    }
}
