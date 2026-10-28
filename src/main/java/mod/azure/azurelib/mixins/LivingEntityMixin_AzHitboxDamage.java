package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_AzHitboxDamage {

    @ModifyVariable(
        method = "damage(Lnet/minecraft/entity/Entity;I)Z",
        at = @At("HEAD"),
        argsOnly = true,
        name = "amount"
    )
    private int azurelib$applyHitboxes(int amount) {
        if (!(this instanceof AzHitboxHolder)) {
            return amount;
        }

        return AzHitboxDamageTracker.applyHurt((LivingEntity) (Object) this, amount);
    }

    @Inject(method = "damage(Lnet/minecraft/entity/Entity;I)Z", at = @At("RETURN"))
    private void azurelib$fireHitboxDamaged(Entity attacker, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof AzHitboxHolder) {
            AzHitboxDamageTracker.fireDamaged((LivingEntity) (Object) this, cir.getReturnValueZ());
        }
    }
}
