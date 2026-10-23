package mod.azure.azurelib.mixins;

import com.google.common.base.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;

@Mixin(EntityArrow.class)
public abstract class EntityArrowMixin_AzHitboxes {

    @Inject(method = "findEntityOnPath", at = @At("HEAD"))
    private void azurelib$beginHitboxQuery(Vec3d start, Vec3d end, CallbackInfoReturnable<Entity> cir) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PROJECTILE);
    }

    @Redirect(
        method = "findEntityOnPath", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/World;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"
        )
    )
    private List<Entity> azurelib$widenCandidateSearch(
        World world,
        Entity except,
        AxisAlignedBB area,
        Predicate<? super Entity> filter
    ) {
        return AzHitboxCollision.findCandidates(area, box -> world.getEntitiesInAABBexcluding(except, box, filter));
    }

    @Redirect(
        method = "findEntityOnPath", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"
        )
    )
    private AxisAlignedBB azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate, candidate.getEntityBoundingBox());
    }

    @Redirect(
        method = "findEntityOnPath", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"
        )
    )
    private RayTraceResult azurelib$clipHitboxes(AxisAlignedBB box, Vec3d from, Vec3d to) {
        return AzHitboxCollision.clip(box, from, to, () -> box.calculateIntercept(from, to));
    }

    @Inject(method = "findEntityOnPath", at = @At("RETURN"))
    private void azurelib$finishHitboxQuery(Vec3d start, Vec3d end, CallbackInfoReturnable<Entity> cir) {
        AzHitboxCollision.finishProjectileQuery(cir.getReturnValue(), (Entity) (Object) this);
    }
}
