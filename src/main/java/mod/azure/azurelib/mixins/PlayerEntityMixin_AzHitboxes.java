package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxInteractions;

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
