package mod.azure.azurelib.common.internal.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import mod.azure.azurelib.common.hitbox.AzHitboxCollision;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin_AzHitboxes {

    @Unique
    private static final String PICK =
        "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;";

    @Unique
    private static final String PROJECTILE =
        "getEntityHitResult(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;";

    @Inject(method = PICK, at = @At("HEAD"))
    private static void azurelib$beginPickQuery(CallbackInfoReturnable<EntityHitResult> cir) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PICK);
    }

    @Inject(method = PROJECTILE, at = @At("HEAD"))
    private static void azurelib$beginProjectileQuery(CallbackInfoReturnable<EntityHitResult> cir) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE);
    }

    @WrapOperation(
        method = { PICK, PROJECTILE }, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"
        )
    )
    private static List<Entity> azurelib$widenCandidateSearch(
        Level level,
        Entity except,
        AABB area,
        Predicate<? super Entity> filter,
        Operation<List<Entity>> original
    ) {
        return AzHitboxCollision.findCandidates(area, box -> original.call(level, except, box, filter));
    }

    @WrapOperation(
        method = { PICK, PROJECTILE }, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;getBoundingBox()Lnet/minecraft/world/phys/AABB;"
        )
    )
    private static AABB azurelib$captureCandidate(Entity candidate, Operation<AABB> original) {
        return AzHitboxCollision.captureCandidate(candidate, original.call(candidate));
    }

    @WrapOperation(
        method = { PICK, PROJECTILE }, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;clip(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;)Ljava/util/Optional;"
        )
    )
    private static Optional<Vec3> azurelib$clipHitboxes(
        AABB box,
        Vec3 from,
        Vec3 to,
        Operation<Optional<Vec3>> original
    ) {
        return AzHitboxCollision.clip(box, from, to, () -> original.call(box, from, to));
    }

    @WrapOperation(
        method = PICK, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/phys/AABB;contains(Lnet/minecraft/world/phys/Vec3;)Z"
        )
    )
    private static boolean azurelib$containsHitboxes(AABB box, Vec3 point, Operation<Boolean> original) {
        return AzHitboxCollision.contains(box, point, () -> original.call(box, point));
    }

    @ModifyReturnValue(method = PROJECTILE, at = @At("RETURN"))
    private static EntityHitResult azurelib$finishProjectileQuery(
        EntityHitResult result,
        @Local(argsOnly = true) Entity projectile
    ) {
        return AzHitboxCollision.finishProjectileQuery(result, projectile);
    }
}
