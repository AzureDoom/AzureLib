package mod.azure.azurelib.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.hitbox.client.AzHitboxClientHooks;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin_AzHitboxAttack {

    @Inject(method = "attack", at = @At("HEAD"))
    private void azurelib$sendHitboxClaim(Player player, Entity entity, CallbackInfo ci) {
        AzHitboxClientHooks.onAttack(player, entity);
    }

    @Inject(method = "interact", at = @At("HEAD"))
    private void azurelib$sendInteractClaim(
        Player player,
        Entity entity,
        EntityHitResult hitResult,
        InteractionHand hand,
        CallbackInfoReturnable<InteractionResult> cir
    ) {
        AzHitboxClientHooks.onInteract(player, entity);
    }
}
