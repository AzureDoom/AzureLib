package mod.azure.azurelib.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;
import mod.azure.azurelib.hitbox.AzHitboxHit;

@Mixin(ProjectileHelper.class)
public abstract class ProjectileHelperMixin_AzHitboxes {

    @Unique
    private static final String FORWARDS_RAYCAST =
        "forwardsRaycast(Lnet/minecraft/entity/Entity;ZZLnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/RayTraceResult;";

    @Inject(method = FORWARDS_RAYCAST, at = @At("HEAD"))
    private static void azurelib$beginHitboxQuery(
        Entity projectile,
        boolean includeEntities,
        boolean ignoreExcludedEntity,
        Entity excludedEntity,
        CallbackInfoReturnable<RayTraceResult> cir
    ) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE);
    }

    @Redirect(
        method = FORWARDS_RAYCAST, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesWithinAABBExcludingEntity(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/List;"
        )
    )
    private static List<Entity> azurelib$widenCandidateSearch(World world, Entity except, AxisAlignedBB area) {
        return AzHitboxCollision.findCandidates(area, box -> world.getEntitiesWithinAABBExcludingEntity(except, box));
    }

    @Redirect(
        method = FORWARDS_RAYCAST, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"
        )
    )
    private static AxisAlignedBB azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate, candidate.getEntityBoundingBox());
    }

    @Redirect(
        method = FORWARDS_RAYCAST, at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"
        )
    )
    private static RayTraceResult azurelib$clipHitboxes(AxisAlignedBB box, Vec3d from, Vec3d to) {
        return AzHitboxCollision.clip(box, from, to, () -> box.calculateIntercept(from, to));
    }

    @Inject(method = FORWARDS_RAYCAST, at = @At("RETURN"), cancellable = true)
    private static void azurelib$finishHitboxQuery(
        Entity projectile,
        boolean includeEntities,
        boolean ignoreExcludedEntity,
        Entity excludedEntity,
        CallbackInfoReturnable<RayTraceResult> cir
    ) {
        RayTraceResult result = cir.getReturnValue();
        Entity hitEntity = result != null ? result.entityHit : null;
        AzHitboxHit hit = AzHitboxCollision.finishProjectileQuery(hitEntity, projectile);

        if (hit != null) {
            cir.setReturnValue(new RayTraceResult(hitEntity, hit.location()));
        }
    }
}
