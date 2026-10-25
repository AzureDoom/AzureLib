package mod.azure.azurelib.mixins;

import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;

/**
 * Replaces 1.7.10's {@code LivingHurtEvent} listener in {@code AzHitboxEvents}: hitbox holders get to rescale the
 * damage at the start of {@code damage(Entity, int)}, before vanilla's invulnerability checks.
 * <p>
 * MCP: {@code EntityLiving#attackEntityFrom(Entity, int)}.
 */
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
}
