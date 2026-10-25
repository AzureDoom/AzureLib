package mod.azure.azurelib.mixins;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;

/**
 * Replaces 1.7.10's {@code LivingHurtEvent} listener in {@code AzHitboxEvents}: hitbox holders get to rescale the
 * damage at the start of {@code damage(Entity, int)}, before vanilla's invulnerability checks.
 */
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
}
