package mod.azure.azurelib.mixins;

import com.google.common.base.Predicate;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import mod.azure.azurelib.hitbox.AzHitboxCollision;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin_AzHitboxPick {

    @Inject(method = "getMouseOver", at = @At("HEAD"))
    private void azurelib$beginPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.begin(AzHitboxCollision.Query.PICK);
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"
        )
    )
    private List<Entity> azurelib$widenCandidateSearch(
        WorldClient world,
        Entity except,
        AxisAlignedBB area,
        Predicate<? super Entity> filter
    ) {
        return AzHitboxCollision.findCandidates(area, box -> world.getEntitiesInAABBexcluding(except, box, filter));
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/Entity;getEntityBoundingBox()Lnet/minecraft/util/math/AxisAlignedBB;"
        )
    )
    private AxisAlignedBB azurelib$captureCandidate(Entity candidate) {
        return AzHitboxCollision.captureCandidate(candidate, candidate.getEntityBoundingBox());
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/AxisAlignedBB;calculateIntercept(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"
        )
    )
    private RayTraceResult azurelib$clipHitboxes(AxisAlignedBB box, Vec3d from, Vec3d to) {
        return AzHitboxCollision.clip(box, from, to, () -> box.calculateIntercept(from, to));
    }

    @Redirect(
        method = "getMouseOver", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/math/AxisAlignedBB;contains(Lnet/minecraft/util/math/Vec3d;)Z"
        )
    )
    private boolean azurelib$containsHitboxes(AxisAlignedBB box, Vec3d point) {
        return AzHitboxCollision.contains(box, point, () -> box.contains(point));
    }

    @Inject(method = "getMouseOver", at = @At("RETURN"))
    private void azurelib$endPick(float partialTicks, CallbackInfo ci) {
        AzHitboxCollision.endQuery();
    }
}
