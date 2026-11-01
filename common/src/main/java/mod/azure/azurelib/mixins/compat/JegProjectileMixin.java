package mod.azure.azurelib.mixins.compat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ttv.migami.jeg.entity.projectile.ProjectileEntity;

import java.util.Optional;

import mod.azure.azurelib.compat.jeg.JegHitboxCompat;

@Pseudo
@Mixin(
    targets = "ttv.migami.jeg.entity.projectile.ProjectileEntity",
    remap = false
)
public abstract class JegProjectileMixin {

    @Inject(
        method = "getHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Lttv/migami/jeg/entity/projectile/ProjectileEntity$EntityResult;",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void azurelib$hitResult(
        Entity target,
        Vec3 from,
        Vec3 to,
        CallbackInfoReturnable<ProjectileEntity.EntityResult> cir
    ) {
        Optional<JegHitboxCompat.Hit> result =
            JegHitboxCompat.resolve(target, from, to);

        if (result == null) {
            return;
        }

        if (result.isEmpty()) {
            cir.setReturnValue(null);
            return;
        }

        JegHitboxCompat.Hit hit = result.get();

        cir.setReturnValue(
            new ProjectileEntity.EntityResult(
                target,
                hit.location(),
                false
            )
        );
    }

    @ModifyArg(
        method = "findEntityOnPath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_6249_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            ordinal = 0,
            remap = false
        ),
        index = 1,
        remap = false
    )
    private AABB azurelib$expandNormalSearch(AABB original) {
        return JegHitboxCompat.expandSearch(original);
    }

    @ModifyArg(
        method = "findEntitiesOnPath",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;m_6249_(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;",
            ordinal = 0,
            remap = false
        ),
        index = 1,
        remap = false
    )
    private AABB azurelib$expandCollateralSearch(AABB original) {
        return JegHitboxCompat.expandSearch(original);
    }

    @Redirect(
        method = "onHitEntity",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;m_6469_(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            remap = false
        ),
        remap = false
    )
    private boolean azurelib$hurtWithHitbox(
        Entity target,
        DamageSource source,
        float damage,
        Entity entity,
        Vec3 hitVec,
        Vec3 startVec,
        Vec3 endVec,
        boolean headshot
    ) {
        JegHitboxCompat.recordDamage(
            (Entity) (Object) this,
            target,
            startVec,
            endVec,
            hitVec
        );

        return target.hurt(source, damage);
    }
}
