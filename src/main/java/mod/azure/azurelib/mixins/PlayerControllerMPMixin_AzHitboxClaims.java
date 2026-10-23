package mod.azure.azurelib.mixins;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.hitbox.client.AzHitboxClientHooks;

@Mixin(PlayerControllerMP.class)
public abstract class PlayerControllerMPMixin_AzHitboxClaims {

    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void azurelib$sendHitboxClaim(EntityPlayer player, Entity target, CallbackInfo ci) {
        AzHitboxClientHooks.onAttack(player, target);
    }

    @Inject(method = "interactWithEntitySendPacket", at = @At("HEAD"))
    private void azurelib$sendInteractClaim(EntityPlayer player, Entity target, CallbackInfoReturnable<Boolean> cir) {
        AzHitboxClientHooks.onInteract(player, target);
    }
}
