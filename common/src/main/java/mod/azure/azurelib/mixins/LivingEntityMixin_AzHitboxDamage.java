package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_AzHitboxDamage {

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, name = "damage")
    private float azurelib$applyHitboxDamage(
        float damage,
        @Local(argsOnly = true, name = "source") DamageSource source
    ) {
        var self = (LivingEntity) (Object) this;

        if (!(self instanceof AzHitboxHolder) || self.level().isClientSide()) {
            return damage;
        }

        return AzHitboxDamageTracker.applyHurt(self, source, damage);
    }
}
