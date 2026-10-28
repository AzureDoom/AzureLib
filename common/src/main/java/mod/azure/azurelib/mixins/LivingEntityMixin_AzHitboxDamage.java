package mod.azure.azurelib.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHit;
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

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void azurelib$captureHitboxDamage(
        ServerLevel level,
        DamageSource source,
        float dmg,
        CallbackInfo ci,
        @Share("azurelib$hitboxHit") LocalRef<AzHitboxHit> hit,
        @Share("azurelib$hitboxAmount") LocalFloatRef accepted
    ) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof AzHitboxHolder) || self.level().isClientSide()) {
            return;
        }

        hit.set(AzHitboxDamageTracker.takeAccepted(self, source));
        accepted.set(dmg);
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void azurelib$fireHitboxDamaged(
        ServerLevel level,
        DamageSource source,
        float dmg,
        CallbackInfo ci,
        @Share("azurelib$hitboxHit") LocalRef<AzHitboxHit> hit,
        @Share("azurelib$hitboxAmount") LocalFloatRef accepted
    ) {
        AzHitboxHit value = hit.get();

        if (value != null) {
            hit.set(null);
            ((AzHitboxHolder) this).onHitboxDamaged(value, source, accepted.get());
        }
    }
}
