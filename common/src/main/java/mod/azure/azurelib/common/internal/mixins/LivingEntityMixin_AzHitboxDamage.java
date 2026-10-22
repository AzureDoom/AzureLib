package mod.azure.azurelib.common.internal.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import mod.azure.azurelib.common.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.common.hitbox.AzHitboxHolder;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin_AzHitboxDamage {

    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true)
    private float azurelib$applyHitboxDamage(float amount, @Local(argsOnly = true) DamageSource source) {
        var self = (LivingEntity) (Object) this;

        if (!(self instanceof AzHitboxHolder) || self.level().isClientSide()) {
            return amount;
        }

        return AzHitboxDamageTracker.applyHurt(self, source, amount);
    }
}
