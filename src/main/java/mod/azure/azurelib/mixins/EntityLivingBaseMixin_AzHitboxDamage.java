package mod.azure.azurelib.mixins;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.azure.azurelib.hitbox.AzHitboxDamageTracker;
import mod.azure.azurelib.hitbox.AzHitboxHolder;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin_AzHitboxDamage {

    @Inject(method = "damageEntity", at = @At("RETURN"))
    private void azurelib$fireHitboxDamaged(DamageSource source, float amount, CallbackInfo ci) {
        EntityLivingBase self = (EntityLivingBase) (Object) this;

        if (self instanceof AzHitboxHolder && !self.worldObj.isRemote) {
            AzHitboxDamageTracker.fireDamaged(self, source);
        }
    }
}
