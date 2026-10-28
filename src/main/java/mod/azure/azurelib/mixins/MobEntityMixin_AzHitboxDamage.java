package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin_AzHitboxDamage {

    @ModifyVariable(
        method = "takeDamage(Lnet/minecraft/entity/Entity;I)Z",
        at = @At("HEAD"),
        argsOnly = true,
        name = "amount"
    )
    private int azurelib$applyHitboxes(int amount) {
        if (!(this instanceof AzHitboxHolder)) {
            return amount;
        }

        return AzHitboxDamageTracker.applyHurt((MobEntity) (Object) this, amount);
    }

    @Inject(method = "takeDamage(Lnet/minecraft/entity/Entity;I)Z", at = @At("RETURN"))
    private void azurelib$fireHitboxDamaged(Entity attacker, int amount, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof AzHitboxHolder) {
            AzHitboxDamageTracker.fireDamaged((MobEntity) (Object) this, cir.getReturnValueZ());
        }
    }
}
